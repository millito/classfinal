package net.roseboy.classfinal.plugin;

import net.roseboy.classfinal.Const;
import net.roseboy.classfinal.JarEncryptor;
import net.roseboy.classfinal.util.StrUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

/**
 * 加密jar/war文件的maven插件
 *
 * @author roseboy
 */
@Mojo(name = "classFinal", defaultPhase = LifecyclePhase.PACKAGE)
public class ClassFinalPlugin extends AbstractMojo {

    //MavenProject
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    //密码
    @Parameter(required = true)
    private String password;
    //机器码
    @Parameter
    private String code;
    //加密的内部-lib/jar名称
    @Parameter
    String libjars;
    //要加密的包名前缀
    @Parameter
    String packages;
    //排除的类名
    @Parameter
    String excludes;
    //外部依赖jarlib
    @Parameter
    String classpath;
    //调试
    @Parameter(defaultValue = "false")
    Boolean debug;

    /**
     * 打包的时候执行
     *
     * @throws MojoExecutionException MojoExecutionException
     * @throws MojoFailureException   MojoFailureException
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        Const.DEBUG = debug;
        Log logger = getLog();
        Build build = project.getBuild();

        long t1 = System.currentTimeMillis();

        String targetJar = build.getDirectory() + File.separator + build.getFinalName()
                + "." + project.getPackaging();
        logger.info("Encrypting " + project.getPackaging() + " [" + targetJar + "]");
        List<String> includeJarList = StrUtils.toList(libjars);
        List<String> packageList = StrUtils.toList(packages);
        List<String> excludeClassList = StrUtils.toList(excludes);
        List<String> classPathList = StrUtils.toList(classpath);
        includeJarList.add("-");

        //加密过程
        JarEncryptor encryptor = new JarEncryptor(targetJar, password.trim().toCharArray(),
                StrUtils.isEmpty(code) ? null : code.trim().toCharArray(),
                packageList, includeJarList, excludeClassList, classPathList);
        String result = encryptor.doEncryptJar();
        long t2 = System.currentTimeMillis();

        logger.info("Encrypt " + encryptor.getEncryptFileCount() + " classes");
        logger.info("Encrypted " + project.getPackaging() + " [" + result + "]");
        logger.info("Encrypt complete");
        logger.info("Time [" + ((t2 - t1) / 1000d) + " s]");
        logger.info("");
    }

}