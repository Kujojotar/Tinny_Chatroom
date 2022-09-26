package com.james.chat.controller;

import com.james.chat.codec.websocket.frame.BinaryWebSocketFrame;
import com.james.chat.codec.websocket.frame.TextWebSocketFrame;
import com.james.chat.dao.*;
import com.james.chat.entity.*;
import com.james.chat.redis.RedisService;
import com.james.chat.result.Result;
import com.james.chat.result.ResultCode;
import com.james.chat.util.ApplicationChannelTracer;
import com.james.chat.util.JacksonUtil;
import com.james.chat.vo.GroupOrUserVo;
import com.james.chat.vo.ListFriendVo;
import io.netty.buffer.Unpooled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class UserInfoController {
    private final ListFriendVoMapper listFriendVoMapper;
    private final GroupMessageMapper groupMessageMapper;
    private final StorageMessageMapper storageMessageMapper;
    private final AppUserMapper appUserMapper;
    private final RedisService redisService;
    private final FriendGroupMapper friendGroupMapper;
    private final FriendRequestMapper friendRequestMapper;

    public UserInfoController(ListFriendVoMapper listFriendVoMapper, GroupMessageMapper groupMessageMapper,
                              StorageMessageMapper storageMessageMapper, RedisService redisService,
                              AppUserMapper appUserMapper, FriendGroupMapper friendGroupMapper,
                              FriendRequestMapper friendRequestMapper) {
        this.listFriendVoMapper = listFriendVoMapper;
        this.groupMessageMapper = groupMessageMapper;
        this.storageMessageMapper = storageMessageMapper;
        this.redisService = redisService;
        this.appUserMapper = appUserMapper;
        this.friendGroupMapper = friendGroupMapper;
        this.friendRequestMapper = friendRequestMapper;
    }

    @GetMapping("/user/b/friends")
    public Result<List<ListFriendVo>> getListFriendViews() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new Result<>(ResultCode.FAILED, false, "认证失败", null);
        }
        String username = (String) authentication.getPrincipal();
        if (username == null) {
            return new Result<>(ResultCode.FAILED, false, "认证失败", null);
        }
        List<ListFriendVo> list = listFriendVoMapper.getFriendVoByUsername(username);
        list.forEach(x->x.setAvartarUrl(listFriendVoMapper.getAvatarUrlByUsername(x.getUsername())));
        return new Result<>(ResultCode.SUCCESS, true, "获取成功", list);
    }

    @GetMapping("/user/b/groups")
    public Result<List<ListFriendVo>> getChatGroupNames() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new Result<>(ResultCode.FAILED, false, "认证失败", null);
        }
        String username = (String) authentication.getPrincipal();
        if (username == null) {
            return new Result<>(ResultCode.FAILED, false, "认证失败", null);
        }
        List<String> results = groupMessageMapper.getUserGroups(username);
        List<ListFriendVo> returnList = results.stream().<ListFriendVo>flatMap(x->{
            ListFriendVo friendVo = new ListFriendVo();
            friendVo.setUsername(x);
            friendVo.setUserNickName("群聊消息");
            return Collections.singleton(friendVo).stream();
        }).collect(Collectors.toList());
        return new Result<>(ResultCode.SUCCESS, true, "获取成功", returnList);
    }

    @GetMapping("/user/b/delay_messages")
    public Result<List<StorageMessage>> getDelayMessages() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new Result<>(ResultCode.FAILED, false, "认证失败", null);
        }
        String username = (String) authentication.getPrincipal();
        if (username == null) {
            return new Result<>(ResultCode.FAILED, false, "认证失败", null);
        }
        List<StorageMessage> res = storageMessageMapper.selectAllMessagesByToIdBefore(username, new Date());
        storageMessageMapper.deleteAllRecordsByToIdBefore(username, new Date());
        return new Result<List<StorageMessage>>(ResultCode.SUCCESS, true, String.valueOf(res.size()), res);
    }

    @GetMapping("/user/b/search_user_or_group")
    public Result<List<GroupOrUserVo>> searchUsersOrGroupByKeyword(@RequestParam("keyword") String keyword, @RequestParam(value = "start",defaultValue = "0") int start) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final int limit = 20;
        List<AppUser> appUserList = appUserMapper.searchUsersWithKeyWordExplicit(keyword, 20, username);
        List<FriendGroup> friendGroups = friendGroupMapper.searchGroupsWithKeywordExplicit(keyword, 20, username);
        List<GroupOrUserVo> res = new ArrayList<>(appUserList.size() + friendGroups.size());
        appUserList.forEach(x->{
            GroupOrUserVo vo = new GroupOrUserVo();
            vo.setType(GroupOrUserVo.VoType.TYPE_USER);
            vo.setName(x.getUsername());
            res.add(vo);
        });
        friendGroups.forEach(x->{
            GroupOrUserVo vo = new GroupOrUserVo();
            vo.setType(GroupOrUserVo.VoType.TYPE_GROUP);
            vo.setName(x.getName());
            res.add(vo);
        });
        return new Result<List<GroupOrUserVo>>(200,true,"获取成功", res);
    }

    @PostMapping("user/b/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("toId") String receiverId, @RequestParam("fileName") String filename) throws IOException {
        if (file != null && !file.isEmpty()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return new Result<>(ResultCode.FAILED, false, "认证失败", null);
            }
            String username = (String) authentication.getPrincipal();
            if (username == null) {
                return new Result<>(ResultCode.FAILED, false, "认证失败", null);
            }
            System.out.println(file.getName());
            System.out.println(receiverId);
            System.out.println(filename);
            if (ApplicationChannelTracer.isUserOnline(receiverId)) {
                ImageTransferObject frame = new ImageTransferObject();
                frame.setToUserId(receiverId);
                frame.setFromUserId(username);
                frame.setTextContent(file.getBytes());
                frame.setToken(username);
                String type = "image/" + (filename.lastIndexOf('.') == -1? "":filename.substring(filename.lastIndexOf('.')+1,filename.length()));
                frame.setFileType(type);
                ApplicationChannelTracer.writeToNioSocketChannelIfPresent(new TextWebSocketFrame(JacksonUtil.writeObject(frame)), receiverId);
            }
            return new Result<>(200,true,"成功","文件上传成功");
        } else {
            return new Result<>(500,false,"失败","文件上传失败");
        }
    }

    @PostMapping("user/b/send_firend_request")
    public Result<String> makeFriendRequest(@RequestBody Map<String,String> args) {
        String fromId = args.get("fromId");
        String toId = args.get("toId");
        String requestMsg = args.get("requestMsg");
        if (fromId == null || toId == null) {
            return new Result<>(500,false,"申请失败", "信息有缺陷");
        }
        if (friendRequestMapper.existsRequest(fromId, toId) || friendRequestMapper.existsFriendship(fromId, toId)) {
            return new Result<>(500,false,"申请失败", "已经发送过请求");
        }
        int res = friendRequestMapper.insertWithNames(fromId, toId, requestMsg);
        return new Result<>(200,true,"申请发送成功","OK");
    }

    @PostMapping("user/b/accept_or_reject")
    public Result<String> acceptOrReject(@RequestBody Map<String,String> args) {
        String res = args.get("attitude");
        String fromId = args.get("fromId");
        String toId = args.get("toId");
        if (res == null || fromId == null || toId == null) {
            return new Result<>(500,false,"申请失败", "信息有缺陷");
        }
        if ("Accept".equalsIgnoreCase(res)) {
            friendRequestMapper.deleteRequestTarget(fromId, toId);
            Long nextId = friendGroupMapper.tmpMaxId();
            friendGroupMapper.insertIntoFriendList(nextId+1, fromId, toId);
        } else {
            friendRequestMapper.deleteRequestTarget(fromId, toId);
        }
        return new Result<>(200,true,"处理成功","OK");
    }

    @GetMapping("user/b/my_requests")
    public Result<List<RequestVo>> getRequestsFromVo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new Result<>(ResultCode.FAILED, false, "认证失败", null);
        }
        String username = (String) authentication.getPrincipal();
        if (username == null) {
            return new Result<>(ResultCode.FAILED, false, "认证失败", null);
        }
        List<FriendRequest> list = friendRequestMapper.getUserRequests(username);
        List<RequestVo> res = list.stream().flatMap(x-> {
            RequestVo vo = new RequestVo();
            vo.setFromUserId(friendRequestMapper.searchName(x.getRequestUser()));
            vo.setToUserId(username);
            vo.setReason(x.getRequestDescription());
            return Collections.<RequestVo>singleton(vo).stream();
        }).collect(Collectors.toList());
        return new Result<>(200,true,"获取成功", res);
    }

}
