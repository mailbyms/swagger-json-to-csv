package com.chunmiao.jsontotxt;

import com.chunmiao.jsontotxt.entity.Root;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

@SpringBootApplication
public class JsonToTxtApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(JsonToTxtApplication.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
        // 获取当前程序所在文件夹的路径
        String folderPath = System.getProperty("user.dir");

        // 创建一个表示文件夹的File对象
        File folder = new File(folderPath);

        // 获取文件夹中所有文件的列表
        File[] files = folder.listFiles();

        // 遍历文件列表
        if (files != null) {
            for (File file : files) {
                String fileName = file.getAbsolutePath();
                // 判断文件是否是以 .json 结尾
                if (file.isFile() && fileName.endsWith(".json")) {
                    // 打印完整文件名
                    System.out.println(fileName);

                    /**
                     * 获得name的顺序，并按顺序写入csv
                     */
                    // 提取文件名的后缀
                    int extensionIndex = fileName.lastIndexOf(".");
                    String tempName = fileName.substring(0, extensionIndex);
                    // 将文件名后缀改为 .csv
                    String csvFileName = tempName + ".csv";
                    // 打印完整文件名
                    System.out.println(file.getAbsolutePath().replace(fileName, csvFileName));


                    json2csv(fileName, csvFileName);
                }
            }
        }

    }

    private static void json2csv(String srcFilename, String dstFilename) throws IOException {
        FileInputStream in = new FileInputStream(srcFilename);
        JsonNode jsonNode = new ObjectMapper().readTree(in);

        /**
         * 取所有数据并存到HashMap中
         */
        String api;
        HashMap<String, List<Root>> hm = new HashMap<>();
        JsonNode node = jsonNode.findValue("paths");
        Iterator<String> stringIterator = node.fieldNames();
        while (stringIterator.hasNext()) {
            JsonNode tags = node.findValue((api = stringIterator.next())); //api
            Iterator<String> methodsname = tags.fieldNames();
            while (methodsname.hasNext()) {
                String method = methodsname.next(); //方法
                JsonNode methods = tags.findValue(method);
                String name = methods.findValue("tags").get(0).asText();
                String description = methods.findValue("summary").asText();

                Root root = new Root(name, method, api,description);  //当前查询到的一个接口数据
                //放到hashmap里管理
                if (hm.containsKey(root.getName())) {
                    List<Root> roots = hm.get(root.getName());
                    roots.add(root);
                    hm.put(root.getName(), roots);
                } else {
                    ArrayList<Root> roots = new ArrayList<>();
                    roots.add(root);
                    hm.put(root.getName(), roots);
                }

            }

        }

        /**
         * 获得name的顺序，并按顺序写入csv
         */
        File file = new File(dstFilename);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(file.toPath()), "GBK"));    //excel不能读取utf-8编码的csv文件

        Iterator<JsonNode> names = jsonNode.findValue("tags").iterator();
        while (names.hasNext()) {
            String name = names.next().findValue("name").asText();
            Iterator<Root> iterator1 = hm.get(name).iterator();
            bufferedWriter.write(name + ",");
            Boolean isFirst = true;
            while (iterator1.hasNext()) {
                if (!isFirst) {
                    bufferedWriter.write(",");
                } else {
                    isFirst = false;
                }
                Root next = iterator1.next();
                bufferedWriter.write(next.getDescription() + "," +
                        next.getApi());
                bufferedWriter.newLine();
            }

        }
        bufferedWriter.close();

//
//        DrugPricLmtRemoteService
//        DrugPubonlnService
//        McsPubonlnService
//        MedinsPrucPlanService
//        MedinsPurcOrdInService
//        bufferedWriter.write(name + ","
//                        + method +","
//                        + api);
//                bufferedWriter.newLine();
//        bufferedWriter.close();
        // Runtime.getRuntime().exec("cmd /c start result.csv");
        //System.out.println("done");
    }
}
