package com.james.chat.codec.ssl;

public class CipherSuite {
    private byte b1;
    private byte b2;

    public CipherSuite(int b1, int b2) {
        this.b1 = (byte)b1;
        this.b2 = (byte)b2;
    }

    public byte getB1() {
        return b1;
    }

    public void setB1(byte b1) {
        this.b1 = b1;
    }

    public byte getB2() {
        return b2;
    }

    public void setB2(byte b2) {
        this.b2 = b2;
    }

    public boolean equals(CipherSuite c) {
        return this.b1 == c.getB1() && this.b2 == c.getB2();
    }

    public static class CipherConstant{
        public static final CipherSuite TLS_NULL_WITH_NULL_NULL = new CipherSuite(0x00,0x00);
        public static final CipherSuite TLS_RSA_WITH_NULL_MD5 = new CipherSuite(0x00,0x01);
        public static final CipherSuite TLS_RSA_WITH_NULL_SHA = new CipherSuite(0x00,0x02);
        public static final CipherSuite TLS_RSA_WITH_NULL_SHA256 = new CipherSuite(0x00,0x3B);
        public static final CipherSuite TLS_RSA_WITH_RC4_128_MD5 = new CipherSuite(0x00,0x04);
        public static final CipherSuite TLS_RSA_WITH_RC4_128_SHA = new CipherSuite(0x00,0x05);
        public static final CipherSuite TLS_RSA_WITH_3DES_EDE_CBC_SHA = new CipherSuite(0x00,0x0A);
        public static final CipherSuite TLS_RSA_WITH_AES_128_CBC_SHA = new CipherSuite(0x00,0x2F);
        public static final CipherSuite TLS_RSA_WITH_AES_256_CBC_SHA = new CipherSuite(0x00,0x35);
        public static final CipherSuite TLS_RSA_WITH_AES_128_CBC_SHA256 = new CipherSuite(0x00,0x3C);
        public static final CipherSuite TLS_RSA_WITH_AES_256_CBC_SHA256 = new CipherSuite(0x00,0x3D);

        public static final CipherSuite TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA      = new CipherSuite(0x00,0x0D);
        public static final CipherSuite TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA      = new CipherSuite(0x00,0x10);
        public static final CipherSuite TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA     = new CipherSuite(0x00,0x13);
        public static final CipherSuite TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA     = new CipherSuite(0x00,0x16);
        public static final CipherSuite TLS_DH_DSS_WITH_AES_128_CBC_SHA       = new CipherSuite(0x00,0x30);
        public static final CipherSuite TLS_DH_RSA_WITH_AES_128_CBC_SHA       = new CipherSuite(0x00,0x31);
        public static final CipherSuite TLS_DHE_DSS_WITH_AES_128_CBC_SHA      = new CipherSuite(0x00,0x32);
        public static final CipherSuite TLS_DHE_RSA_WITH_AES_128_CBC_SHA      = new CipherSuite(0x00,0x33);
        public static final CipherSuite TLS_DH_DSS_WITH_AES_256_CBC_SHA       = new CipherSuite(0x00,0x36);
        public static final CipherSuite TLS_DH_RSA_WITH_AES_256_CBC_SHA       = new CipherSuite(0x00,0x37);
        public static final CipherSuite TLS_DHE_DSS_WITH_AES_256_CBC_SHA      = new CipherSuite(0x00,0x38);
        public static final CipherSuite TLS_DHE_RSA_WITH_AES_256_CBC_SHA      = new CipherSuite(0x00,0x39);
        public static final CipherSuite TLS_DH_DSS_WITH_AES_128_CBC_SHA256    = new CipherSuite(0x00,0x3E);
        public static final CipherSuite TLS_DH_RSA_WITH_AES_128_CBC_SHA256    = new CipherSuite(0x00,0x3F);
        public static final CipherSuite TLS_DHE_DSS_WITH_AES_128_CBC_SHA256   = new CipherSuite(0x00,0x40);
        public static final CipherSuite TLS_DHE_RSA_WITH_AES_128_CBC_SHA256   = new CipherSuite(0x00,0x67);
        public static final CipherSuite TLS_DH_DSS_WITH_AES_256_CBC_SHA256    = new CipherSuite(0x00,0x68);
        public static final CipherSuite TLS_DH_RSA_WITH_AES_256_CBC_SHA256    = new CipherSuite(0x00,0x69);
        public static final CipherSuite TLS_DHE_DSS_WITH_AES_256_CBC_SHA256   = new CipherSuite(0x00,0x6A);
        public static final CipherSuite TLS_DHE_RSA_WITH_AES_256_CBC_SHA256   = new CipherSuite(0x00,0x6B);

        public static final CipherSuite TLS_DH_anon_WITH_RC4_128_MD5          = new CipherSuite(0x00,0x18);
        public static final CipherSuite TLS_DH_anon_WITH_3DES_EDE_CBC_SHA     = new CipherSuite(0x00,0x1B);
        public static final CipherSuite TLS_DH_anon_WITH_AES_128_CBC_SHA      = new CipherSuite(0x00,0x34);
        public static final CipherSuite TLS_DH_anon_WITH_AES_256_CBC_SHA      = new CipherSuite(0x00,0x3A);
        public static final CipherSuite TLS_DH_anon_WITH_AES_128_CBC_SHA256   = new CipherSuite(0x00,0x6C);
        public static final CipherSuite TLS_DH_anon_WITH_AES_256_CBC_SHA256   = new CipherSuite(0x00,0x6D);
        public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 = new CipherSuite(0xc0,0x2f);
    }
}
