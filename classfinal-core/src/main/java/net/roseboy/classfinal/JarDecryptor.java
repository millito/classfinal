package net.roseboy.classfinal;

import net.roseboy.classfinal.util.*;

import java.io.File;

/**
 * java class解密
 *
 * @author roseboy
 */
public class JarDecryptor {
    private char[] code;//机器码

    public JarDecryptor() {
        code = SysUtils.makeMarchinCode();
    }

    /**
     * 根据名称解密出一个文件
     *
     * @param projectPath 项目所在的路径
     * @param className   文件名
     * @param password    密码
     * @return 解密后的字节
     */
    public byte[] doDecrypt(String projectPath, String className, char[] password) {
        long t1 = System.currentTimeMillis();
        File workDir = new File(projectPath);

        String fileName = "META-INF/" + Const.FILE_NAME + "/" + className;
        byte[] bytes = readFile(workDir, fileName);
        if (bytes == null) {
            return null;
        }

        //读取机器码
        byte[] codeBytes = readFile(workDir, "META-INF/" + Const.FILE_NAME + "/" + Const.CONFIG_CODE);

        //无密码启动,读取隐藏的密码
        if (password.length == 1 && password[0] == '#') {
            password = readPassFromJar(workDir);
        }

        //有机器码，先用机器码解密
        if (codeBytes != null) {
            //本机器码和打包的机器码不匹配
            if (!StrUtils.equal(EncryptUtils.md5(this.code), StrUtils.toChars(codeBytes))) {
                Log.println("该项目不可在此机器上运行!\n");
                System.exit(-1);
            }

            //用机器码解密
            char[] pass = StrUtils.merger(className.toCharArray(), code);
            bytes = EncryptUtils.de(bytes, pass, Const.ENCRYPT_TYPE);
        }

        //密码解密
        char[] pass = StrUtils.merger(password, className.toCharArray());
        bytes = EncryptUtils.de(bytes, pass, Const.ENCRYPT_TYPE);

        long t2 = System.currentTimeMillis();
        Log.debug("解密: " + className + " (" + (t2 - t1) + " ms)");

        return bytes;

    }

    /**
     * 在jar文件或目录中读取文件字节
     *
     * @param workDir jar文件或目录
     * @param name    文件名
     * @return 文件字节数组
     */
    public static byte[] readFile(File workDir, String name) {
        byte[] bytes = null;
        //jar文件
        if (workDir.isFile()) {
            bytes = JarUtils.getFileFromJar(workDir, name);
        } else {//war解压的目录
            File file = new File(workDir, name);
            if (file.exists()) {
                bytes = IoUtils.readFileToByte(file);
            }
        }
        return bytes;
    }

    /**
     * 读取隐藏在jar的密码
     *
     * @param workDir jar路径
     * @return 密码char
     */
    public static char[] readPassFromJar(File workDir) {
        byte[] passbyte = readFile(workDir, "META-INF/" + Const.FILE_NAME + "/" + Const.CONFIG_PASS);
        if (passbyte != null) {
            char[] pass = StrUtils.toChars(passbyte);
            return EncryptUtils.md5(pass);
        }
        return null;
    }
}
