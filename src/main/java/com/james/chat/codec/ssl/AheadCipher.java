package com.james.chat.codec.ssl;

public class AheadCipher {
    byte[] nonce_explicit;
    byte[] content;

    public AheadCipher(byte[] nonce_explicit, byte[] content) {
        this.nonce_explicit = nonce_explicit;
        this.content = content;
    }

    public byte[] getNonce_explicit() {
        return nonce_explicit;
    }

    public void setNonce_explicit(byte[] nonce_explicit) {
        this.nonce_explicit = nonce_explicit;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
