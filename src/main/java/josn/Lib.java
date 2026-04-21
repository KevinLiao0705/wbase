/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package josn;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 通用工具函式庫，提供檔案操作、JSON 處理、網路、ZIP 壓縮等常用功能。
 *
 * @author kevin
 */
public class Lib {

    public static int ptLevel = 3;
    public static List<String> lsClassName;
    public static List<String> lsClassData;
    public static String errString;

    static int error_f = 0;
    static int valueInt = 0;
    static long valueLong = 0;
    static float valueFloat = 0;
    static double valueDouble = 0;
    static int valueType = 0;//0: none, 1: int, 2: long, 3:float, 4:double

    static String retstr;
    static char[] asciiTbl = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    static int reti;
    static float retf;
    static String rets;
    static ArrayList<String> retsal = new ArrayList<>();
    static int[] retia;
    static float[] retfa;

    /**
     * 執行系統指令（不等待結果）。
     *
     * @param exestr 要執行的指令字串
     * @return 成功回傳 0，失敗回傳 1
     */
    public static int exe(String exestr) {
        try {
            Runtime.getRuntime().exec(exestr);
            return 0;
        } catch (IOException ex) {
            Logger.getLogger(Lib.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
            return 1;
        }
    }
    /**
     * 檢查指定名稱的行程是否正在執行中。
     *
     * @param processName 要檢查的行程名稱
     * @return 存在回傳 1，不存在回傳 0
     */
    public static int chkProcessExist(String processName) {
            String exeCmdStr="tasklist";
            if (GB.osName.equals("linux")) {
                exeCmdStr="ps -ef";
            }
            String retStr=Lib.exeCmdRet(exeCmdStr);
            if(retStr!=null){
                String[] strA=retStr.split("\n");
                for(int i=0;i<strA.length;i++){
                    if(strA[i].contains(processName))
                        return 1;
                }
            }
            return 0;    
        
        
    }
    /**
     * 執行系統指令並回傳執行結果字串。
     *
     * @param cmd 要執行的指令
     * @return 指令輸出字串，失敗時回傳 null
     */
    public static String exeCmdRet(String cmd) {
        String retStr = null;
        String[] command=new String[]{"cmd.exe", "/c",""};
        if (GB.osName.equals("linux")) {
            command=new String[]{"sh", "-c",""};
        }
        command[2]=cmd;
        try {
            // Create a ProcessBuilder instance
            ProcessBuilder builder = new ProcessBuilder(command);
            // Optionally, redirect error stream to output stream
            builder.redirectErrorStream(true);
            // Start the process
            Process process = builder.start();
            // Read the output from the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            // Wait for the process to complete
            int exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);
            return output.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return retStr;
    }

    /**
     * 使用串流（Stream）方式複製檔案。
     *
     * @param source 來源檔案
     * @param dest   目標檔案
     * @throws IOException 讀寫發生錯誤時拋出
     */
    static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    /**
     * 從 paraSet.json 讀取整數型設定值。
     *
     * @param keyName JSON 鍵名
     * @param errOut  讀取失敗時的預設回傳值
     * @return 讀取到的整數值，失敗時回傳 errOut
     */
    static int readParaSet(String keyName, int errOut) {
        int ibuf = errOut;
        try {
            String content = Lib.readFile("paraSet.json");
            JsonObject jsPara = JsonParser.parseString(content).getAsJsonObject();
            ibuf = jsPara.get(keyName).getAsInt();
            return ibuf;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ibuf;
    }

    /**
     * 將指定鍵的整數值在 paraSet.json 中做 XOR 1 切換（toggle）並寫回檔案。
     *
     * @param keyName 要切換的 JSON 鍵名
     * @param ibuf    初始值（實際寫入值由檔案讀取後 XOR 決定）
     */
    static void writeParaSet(String keyName, int ibuf) {
        try {
            Gson gson = new Gson();
            String content = Lib.readFile("paraSet.json");
            JsonObject jsPara = JsonParser.parseString(content).getAsJsonObject();
            ibuf = jsPara.get(keyName).getAsInt();
            ibuf ^= 1;
            jsPara.add(keyName, gson.toJsonTree(ibuf));
            content = jsPara.toString();
            BufferedWriter outf = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("paraSet.json"), "UTF-8"));
            try {
                outf.write(content);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                outf.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 將字串以雙空格為分隔符號切割成字串陣列。
     *
     * @param _istr 輸入字串
     * @return 分割後的字串陣列
     */
    static String[] splitSeg(String _istr) {
        String istr = _istr.trim();
        int len = istr.length();
        List<String> list = new ArrayList<String>();
        String nstr = "";
        int start_f = 0;
        for (int i = 0; i < len; i++) {
            char ch = istr.charAt(i);
            if (ch == ' ') {
                if (start_f == 0) {
                    continue;
                }
                if (istr.charAt(i + 1) == ' ') {
                    list.add(nstr);
                    start_f = 0;
                    continue;
                }
                nstr += ch;
                continue;
            } else {
                if (start_f == 0) {
                    start_f = 1;
                    nstr = "";
                }
                nstr += ch;
            }
        }
        if (start_f == 1) {
            list.add(nstr);
        }
        return list.toArray(new String[0]);

    }

    /**
     * 將鍵值對放入 JSONObject，若失敗不拋出例外。
     *
     * @param jobj 目標 JSONObject
     * @param key  鍵名
     * @param obj  值
     * @return 成功回傳 0，失敗回傳 -1
     */
    static int jsonPut(JSONObject jobj, String key, Object obj) {
        try {
            jobj.put(key, obj);
            return 0;
        } catch (Exception ex) {
            return -1;
        }

    }

    /**
     * 檢查指定 IP 是否可達（ICMP 可達性檢查）。
     *
     * @param ip       目標 IP 位址
     * @param wait_tim 等待超時（毫秒）
     * @return 可達回傳 0，不可達回傳 -1
     */
    static public int ping(String ip, int wait_tim) {
        try {
            if (InetAddress.getByName(ip).isReachable(wait_tim)) {
                return 0;
            } else {
                return -1;
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * 依百作業系統自動選擇 Ping 方式，Windows 使用可達性檢查，Linux 使用 ping 指令。
     * 成功回傳 0，失敗回傳 1。
     *
     * @param hostname 目標主機名稱或 IP
     * @return 成功回傳 0，失敗回傳 1 或 -1
     */
    public static final int ping(String hostname) {
        try {
            if (GB.osInx == 0) //win n=tx count w=wait time
            {
                //return Runtime.getRuntime().exec("ping -n 1 -w 1000 " + hostname).waitFor();  //windows
                return ping(hostname, 1000);
            }
            if (GB.osInx == 1) {//linux
                return Runtime.getRuntime().exec("ping -c 1 " + hostname).waitFor();  //linux
            }
            return 1;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 使用 Linux ping 指令檢查主機是否可達。
     *
     * @param hostname 目標主機名稱或 IP
     * @return 成功回傳 0，失敗回傳非零或 -1
     */
    public static final int linuxPing(String hostname) {
        try {
            return Runtime.getRuntime().exec("ping -c 1 " + hostname).waitFor();  //linux
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 讀取檔案內容為 UTF-8 字串。
     *
     * @param path 檔案路徑
     * @return 檔案內容字串
     * @throws IOException 讀取發生錯誤時拋出
     */
    static String readFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return new String(bytes, 0, bytes.length, "UTF-8");
    }

    /**
     * 建立標準 actResponse JSON 字串（不含內容欄位）。
     *
     * @param act    動作名稱
     * @param status 回應狀態
     * @param cmdInx 指令索引
     * @return JSON 格式字串
     */
    static String actResponse(String act, String status, int cmdInx) {
        String str = "{";
        str += "\"act\":\"actResponse\"";
        str += ", \"actName\":\"" + act + "\"";
        str += ", \"status\":\"" + status + "\"";
        str += ", \"cmdInx\":" + cmdInx;
        str += ", \"reti\":1";
        str += "}";
        return str;
    }

    /**
     * 建立含 content 欄位的 actResponse JSON 字串。
     *
     * @param act     動作名稱
     * @param status  回應狀態
     * @param cmdInx  指令索引
     * @param content 回傳內容
     * @return JSON 格式字串
     */
    static String actResponseContent(String act, String status, int cmdInx, String content) {
        String str = "{";
        str += "\"act\":\"actResponse\"";
        str += ", \"actName\":\"" + act + "\"";
        str += ", \"status\":\"" + status + "\"";
        str += ", \"cmdInx\":" + cmdInx;
        str += ", \"reti\":1";
        str += ", \"content\":\"" + content + "\"";
        str += "}";
        return str;
    }

    /**
     * 將一個位元組轉換為兩位十六進制字串。
     *
     * @param bt 要轉換的位元組
     * @return 兩位字元的十六進制字串，如 "FF"
     */
    public static String byteToHexString(byte bt) {
        String str = "";
        str += Lib.asciiTbl[(bt >> 4) & 15];
        str += Lib.asciiTbl[bt & 15];
        return str;
    }

    /**
     * 將字串依指定格式轉換為位元組陣列。
     * 支援 "ASCII" 以及 "HEX" （十六進制格式，如 "FF 00 1A"）。
     *
     * @param str  輸入字串
     * @param type 格式類型："ASCII" 或 "HEX"
     * @return 轉換後的位元組陣列，格式錯誤或空字串回傳 null
     */
    public static byte[] toBytes(String str, String type) {
        if (str.length() == 0) {
            return null;
        }
        if (type.equals("ASCII")) {
            return str.getBytes();
        }

        if (type.equals("HEX")) {
            int[] ints = new int[4096];
            byte[] bts = str.getBytes();
            int binx = 0;
            int valueInx = 0;
            for (int i = 0; i < bts.length; i++) {
                if (bts[i] == ' ' || bts[i] == ',') {
                    binx = 0;
                    continue;
                }
                binx++;
                int value;
                if (binx == 2) {
                    if (bts[i - 1] == '0' && bts[i] == 'x') {
                        binx = 0;
                        continue;
                    }
                    if (bts[i - 1] == '0' && bts[i] == 'X') {
                        binx = 0;
                        continue;
                    }
                    binx = 0;
                    value = 0;
                    for (int j = 0; j < 2; j++) {
                        value = value * 16;
                        byte bt = bts[i + j - 1];
                        if (bt >= '0' && bt <= '9') {
                            value += bt - '0';
                            continue;
                        }
                        if (bt >= 'a' && bt <= 'f') {
                            value += bt - 'a' + 10;
                            continue;
                        }
                        if (bt >= 'A' && bt <= 'F') {
                            value += bt - 'A' + 10;
                            continue;
                        }
                        return null;
                    }
                    ints[valueInx] = value;
                    valueInx++;
                }

            }
            byte[] retBytes = new byte[valueInx];
            for (int i = 0; i < retBytes.length; i++) {
                retBytes[i] = (byte) ints[i];
            }
            return retBytes;

        }
        return null;
    }

    /**
     * 將逗號分隔的十六進制字串（如 "FF,0A,1B"）轉換為位元組陣列。
     *
     * @param str 逗號分隔的十六進制字串
     * @return 對應的位元組陣列
     */
    public static byte[] toHexBytes(String str) {
        if (str.length() == 0) {
            return null;
        }
        String[] strA = str.split(",");
        byte[] retBytes = new byte[strA.length];

        for (int i = 0; i < strA.length; i++) {
            int ibuf = Integer.parseInt(strA[i], 16);
            retBytes[i] = (byte) ibuf;
        }
        return retBytes;
    }

    /**
     * 將訊息輸出至標準輸出。
     *
     * @param inf 要輸出的訊息
     */
    public static void log(String inf) {
        System.out.println(inf);
    }

    /**
     * 級別 1 輸出（仅當 ptLevel &lt; 1 時印出，難度最高）。
     *
     * @param inf 要輸出的訊息
     */
    public static void lp1(String inf) {
        if (ptLevel < 1) {
            System.out.println(inf);
        }
    }

    /**
     * 級別 2 輸出（仅當 ptLevel &lt; 2 時印出，中等難度）。
     *
     * @param inf 要輸出的訊息
     */
    public static void lp2(String inf) {
        if (ptLevel < 2) {
            System.out.println(inf);
        }
    }

    /**
     * 級別 3 輸出（仅當 ptLevel &lt; 3 時印出，難度最低）。
     *
     * @param inf 要輸出的訊息
     */
    public static void lp3(String inf) {
        if (ptLevel < 3) {
            System.out.println(inf);
        }
    }

    /**
     * 將鍵值對安全地放入 JSONObject，若發生例外則印出 Stack Trace。
     *
     * @param jo    目標 JSONObject
     * @param key   鍵名
     * @param value 值
     */
    public static void putJos(JSONObject jo, String key, Object value) {
        try {
            //jo.accumulate(key, value);  //if exist trans to array
            jo.put(key, value);//添加元素
            //jo.append(key, value);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 建立安全的解壓縮目標檔案物件，防止 Zip Slip 路徑穿越攻擊。
     *
     * @param destinationDir 解壓縮目標目錄
     * @param zipEntry       ZIP 項目
     * @return 目標檔案物件
     * @throws IOException 路徑穿越時拋出
     */
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    /**
     * 解壓縮指定 ZIP 檔案到目標目錄。
     *
     * @param zipName  ZIP 檔案路徑
     * @param _destDir 目標解壓縮目錄
     * @throws IOException 解壓縮發生錯誤時拋出
     */
    public static void unzipFile(String zipName, String _destDir) throws IOException {
        String fileZip = zipName;
        File destDir = new File(_destDir);
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
        }
    }

    /**
     * 將整個目錄壓縮為 ZIP 檔案。
     *
     * @param dirName 要壓縮的目錄路徑
     * @param zipName 輸出 ZIP 檔案路徑
     * @throws IOException 壓縮發生錯誤時拋出
     */
    public static void zipDir(String dirName, String zipName) throws IOException {
        //dirName="dirName";
        //zipName="dirCompressed.zip";
        String sourceFile = dirName;
        FileOutputStream fos = new FileOutputStream(zipName);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);
        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
    }

    /**
     * 遞迴壓縮單一檔案或目錄到 ZipOutputStream。
     * 自動跳過就算小檔案和指定延伸符號（kvzip、kvbin）。
     *
     * @param fileToZip 要壓縮的檔案或目錄
     * @param fileName  ZIP 內部路徑名稱
     * @param zipOut    目標 ZipOutputStream
     * @throws IOException 發生錯誤時拋出
     */
    public static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {

        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                String zipFile = childFile.getName();
                String[] strA = zipFile.split("\\.");
                if (strA.length == 2) {
                    if (strA[1].equals("kvzip")) {
                        continue;
                    }
                    if (strA[1].equals("kvbin")) {
                        continue;
                    }
                }
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }

        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    /**
     * 讀取 XML 設定檔中指定節點的所有屬性到 HashMap。
     *
     * @param fileName 設定 XML 檔案路徑
     * @param strType  propertie 節點的 name 屬性對應小樣名稱
     * @return 鍵對大寫的 HashMap
     * @throws Exception 解析 XML 發生錯誤時拋出
     */
    public static HashMap<String, String> XMLMap(String fileName, String strType) throws Exception {
        HashMap<String, String> mapXML = new HashMap<String, String>();
        File file = new File(fileName);
        // 建立工廠
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 建立解析器
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        // 建立 Xpath 
        XPath xPath = XPathFactory.newInstance().newXPath();
        // 設定節點 Xpath
        String expression = "/properties/propertie[@name='" + strType + "']/item"; // 路徑
        // 取得結點資料
        NodeList nodelist = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

        // 讀取properties.xml放入MAP
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node node = nodelist.item(i);
            Element element = (Element) node;
            mapXML.put(element.getAttribute("key"), element.getAttribute("value"));
        }
        return mapXML;
    }

    /**
     * 示範方法：讀取 properties.xml 並修改 Login/USER 的屬性對應寫入檔案。
     *
     * @throws Exception 發生錯誤時拋出
     */
    public static void WriteSomthing() throws Exception {
        // 度取資料
        String strDir = System.getProperty("user.dir");
        File file = new File(strDir + "\\properties.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder(); // 建立解析器
        Document doc = builder.parse(file);
        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = "/properties/propertie[@name='Login']/item[@key='USER']"; // 路徑
        NodeList node = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        // 取出 element 
        Element element = (Element) node.item(0);

        // 寫入檔案
        // 修改參數
        element.setAttribute("value", "PedroTest");
        // 建立轉換器
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        // source 不一定是整份檔案，只要 xml 格式正確可以是子階樹結構
        DOMSource source = new DOMSource(doc);
        // 寫入的檔案
        FileWriter writer = new FileWriter(new File(file.getPath()));
        // 可以用 java.io.Writer or java.io.OutputStream 來創建 StreamResult.
        StreamResult result = new StreamResult(writer);
        // 寫入原來的檔案
        transformer.transform(source, result);
        // // 寫入到其他路徑
        // StreamResult resultToFile = new StreamResult(new File("C:/temp/XMLFromPathValue.xml"));
        // transformer.transform(source, resultToFile);
    }

    /**
     * 將 JSON 字串轉換為 JavaScript 變數賦値格式的字串（key=value 每行一筆）。
     *
     * @param inStr 輸入 JSON 字串
     * @return 轉換後的字串，格式錯誤時回傳 null
     */
    public String josnToJs(String inStr) {

        String outStr = "";
        JSONObject jobj;
        String key;
        Object value;
        String str;
        int len = inStr.length();
        int fg = 0;
        for (int i = 0; i < len; i++) {
            if (inStr.charAt(i) == '{') {
                fg = 1;
                break;
            }
            if (inStr.charAt(i) == '[') {
                fg = 2;
                break;
            }
        }
        try {
            if (fg == 0) {
                Lib.errString = "Json Format First frror !!!";
                return null;
            }
            if (fg == 1) {
                jobj = new JSONObject(inStr);
                Iterator<String> it = jobj.keys();
                int first = 0;
                while (it.hasNext()) {
                    key = it.next();
                    if (first != 0) {
                        outStr += ',';
                    }
                    first = 1;
                    value = jobj.get(key);
                    str = value.toString();

                    for (int j = 0; j < str.length(); j++) {
                        if (value.getClass().getSimpleName().equals("String")) {
                            value = "\"" + value.toString() + "\"";
                        }
                    }
                    outStr += "\n" + key + "=" + value;
                }

            }
        } catch (Exception ex) {
            Lib.errString = ex.toString();
            return null;

        }

        return outStr;

    }

    /**
     * 以預設字元集讀取檔案內容為字串。
     *
     * @param fileName 檔案路徑
     * @return 檔案內容字串，失敗時回傳空字串
     */
    public static String fileToString(String fileName) {
        try {
            String contents;
            contents = new String(Files.readAllBytes(Paths.get(fileName)));
            //return new String(contents.getBytes("utf-8"),"utf-8");
            return contents;
        } catch (IOException ex) {
            Logger.getLogger(Lib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    /**
     * 以 UTF-8 編碼逐行讀取檔案內容為字串。
     *
     * @param fileName 檔案路徑
     * @return 檔案內容字串，失敗時回傳 null
     */
    public static String readStringFile(String fileName) {
        String content = "";
        int first = 0;
        try {
            File file = new File(fileName);
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader reader = new BufferedReader(isr);
            //BufferedReader reader = new BufferedReader(new FileReader(file));
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (first == 1) {
                    content += "\n";
                }
                first = 1;
                content += currentLine;
            }
            reader.close();
            return content;
        } catch (Exception ex) {
            Lib.errString = ex.toString();
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 將字串內容以 UTF-8 編碼儲存到檔案。
     *
     * @param _fileName 檔案路徑
     * @param inStr     要寫入的內容
     * @return 成功回傳 true，失敗回傳 false
     */
    public static boolean saveFile(String _fileName, String inStr) {
        String fileName = _fileName;
        FileOutputStream outfile;
        try {
            outfile = new FileOutputStream(fileName);
            outfile.write(inStr.getBytes("utf-8"));
            outfile.close();
            return true;
        } catch (FileNotFoundException ex) {
            Lib.log("FileNotFound: " + fileName);
            return false;
        } catch (IOException ex) {
            Lib.log("SaveFileError: " + fileName);
            return false;
        }
    }
    //=======================================================================

    /**
     * 遞迴刪除目錄及其下所有內容（跨過符號連結）。
     *
     * @param file 要刪除的檔案或目錄
     */
    public static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }

    /**
     * 使用 FileChannel 高效複製檔案。
     *
     * @param sourceName 來源檔案路徑
     * @param destName   目標檔案路徑
     * @return 成功回傳 0，失敗回傳 1
     */
    public static int copyFile(String sourceName, String destName) {
        File source = new File(sourceName);
        File dest = new File(destName);

        int err = 0;
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(dest)) {
            FileChannel inputChannel = fis.getChannel();
            FileChannel outputChannel = fos.getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } catch (IOException ex) {
            Lib.log("CopuFileError: " + source + " -> " + dest);
            err = 1;
        }
        return err;
    }

    /**
     * 將字串輸出至標準輸出（System.out.println 別名）。
     *
     * @param str 要輸出的字串
     */
    public static void prt(String str) {
        System.out.println(str);
    }

    /**
     * 將字串轉換為 long 型別。
     *
     * @param str       輸入字串
     * @param errorCode 轉換失敗時的預設回傳値
     * @return 轉換後的 long 値
     */
    public static long str2long(String str, long errorCode) {
        long lg = errorCode;
        try {
            lg = Long.parseLong(str);
        } catch (NumberFormatException ex) {
            System.out.println("parseLong Error line");
        }
        return lg;
    }

    /**
     * 將字串轉換為 int，並限制在最大値與最小値範圍內。
     *
     * @param str       輸入字串
     * @param default_i 超出範圍或轉換失敗時的預設値
     * @param max       允許的最大値
     * @param min       允許的最小値
     * @return 轉換後的 int 値
     */
    public static int str2int(String str, int default_i, int max, int min) {
        try {

            int ibuf = Integer.parseInt(str);
            if (ibuf > max) {
                return default_i;
            }
            if (ibuf < min) {
                return default_i;
            }
            return ibuf;
        } catch (NumberFormatException e) {
            return default_i;
        }
    }

    /**
     * 將字串轉換為 int。
     *
     * @param str       輸入字串
     * @param errorCode 轉換失敗時的預設回傳値
     * @return 轉換後的 int 値
     */
    public static int str2int(String str, int errorCode) {
        int lg = errorCode;
        try {
            lg = Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            System.out.println("parseLong Error line");
        }
        return lg;
    }

    /**
     * 將字串轉換為 float。
     *
     * @param str       輸入字串
     * @param errorCode 轉換失敗時的預設回傳値
     * @return 轉換後的 float 値
     */
    public static float str2float(String str, float errorCode) {
        float lg = errorCode;
        try {
            lg = Float.parseFloat(str);
        } catch (NumberFormatException ex) {
            System.out.println("parseFlat Error line");
        }
        return lg;
    }

    /**
     * 安全地從 JSONObject 取得指定鍵的對應元素，不存在時回傳 null。
     *
     * @param jobj JSONObject 物件
     * @param key  鍵名
     * @return 對應元素，不存在時回傳 null
     */
    static Object getJson(JSONObject jobj, String key) {
        try {
            return jobj.get(key);//添加元素
        } catch (JSONException ex) {
        }
        return null;
    }

    /**
     * 從檔案中讀取指定函式名稱對應的函式內容字串。
     *
     * @param fileName 目標檔案路徑
     * @param funcName 要搜尋的函式名稱
     * @return 函式完整內容字串，找不到時回傳空字串
     */
    public static String fileFunc2str(String fileName, String funcName) {
        char ch;
        char[] chs;
        String str = "";
        FileReader fr;
        int index = 0;
        chs = funcName.toCharArray();
        char[] cbuf = new char[chs.length];
        int cbuf_len = 0;
        int cbuf_inx1 = 0;
        int cbuf_inx2 = 0;
        int ibuf;

        try (FileReader fr_res = new FileReader(fileName)) {
            fr = fr_res;
            //===========================================
            for (;;) {
                if (cbuf_len > 0) {
                    ch = cbuf[cbuf_inx2++];
                    cbuf_len--;
                } else {
                    ibuf = fr.read();
                    if (ibuf == -1) {
                        break;
                    }
                    ch = (char) ibuf;
                    cbuf[cbuf_inx1++] = ch;
                }
                if (ch == chs[index++]) {
                    if (index == chs.length) {
//==============================================================================                            
                        int func_start_f = 0;
                        int mark_start_f = 0;
                        int quot_cnt = 0;
                        String func_str = "";

                        for (;;) {
                            ibuf = fr.read();
                            if (ibuf == -1) {
                                return "";
                            }
                            ch = (char) ibuf;
                            func_str += ch;
                            if (mark_start_f == 1) {
                                if (ch == '/') {
                                    mark_start_f = 2;
                                    continue;
                                }
                                if (ch == '*') {
                                    mark_start_f = 3;
                                    continue;
                                }
                                mark_start_f = 0;
                            }
                            if (mark_start_f == 2) {
                                if (ch == '\n') {
                                    mark_start_f = 0;
                                }
                                continue;
                            }
                            if (mark_start_f == 3) {
                                if (ch == '*') {
                                    mark_start_f = 4;
                                }
                                continue;
                            }
                            if (mark_start_f == 4) {
                                if (ch == '/') {
                                    mark_start_f = 0;
                                    continue;
                                }
                                mark_start_f = 3;
                                continue;
                            }
                            if (ch == '/') {
                                mark_start_f = 1;
                                continue;
                            }
                            if (func_start_f == 0) {
                                if (ch == '{') {
                                    func_start_f = 1;
                                    quot_cnt = 1;
                                }
                                continue;
                            }
                            if (ch == '{') {
                                quot_cnt++;
                            }
                            if (ch == '}') {
                                quot_cnt--;
                                if (quot_cnt == 0) {
                                    return funcName + func_str;

                                }
                            }
                        }
//==============================================================================                            
                    }
                } else {
                    if (cbuf_inx1 > 0) {
                        cbuf_len = cbuf_inx1 - 1;
                        cbuf_inx2 = 1;
                    }
                    index = 0;
                    cbuf_inx1 = 0;
                }
            }
            return "";
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Lib.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Lib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return str;
    }

    /**
     * 從檔案中讀取兩個相同分隔符號之間的資料內容。
     *
     * @param fileName 目標檔案路徑
     * @param sepstr   分隔字串（實現為找到第一個後再找第二個，將兩者之間的內容回傳）
     * @return 分隔字串之間的內容，找不到時回傳空字串
     */
    public static String fileData2str(String fileName, String sepstr) {
        int ibuf;
        char ch;
        char[] chs;
        String str = "";
        FileReader fr;
        chs = sepstr.toCharArray();
        char[] cbuf = new char[chs.length];
        int index = 0;
        int cbuf_len = 0;
        int cbuf_inx1 = 0;
        int cbuf_inx2 = 0;
        int same_cnt = 0;
        String middata_str = "";
        try {
            fr = new FileReader(fileName);
            try {
                //===========================================
                for (;;) {
                    if (cbuf_len > 0) {
                        ch = cbuf[cbuf_inx2++];
                        cbuf_len--;
                    } else {
                        ibuf = fr.read();
                        if (ibuf == -1) {
                            break;
                        }
                        ch = (char) ibuf;
                        cbuf[cbuf_inx1++] = ch;
                    }
                    if (same_cnt == 1) {
                        middata_str += ch;
                    }
                    if (ch == chs[index++]) {
//==============================================================================                            
                        if (index == chs.length) {
                            if (same_cnt == 0) {
                                index = 0;
                                cbuf_len = 0;
                                cbuf_inx1 = 0;
                                cbuf_inx2 = 0;
                                same_cnt++;
                            } else {
                                fr.close();
                                return middata_str.substring(0, middata_str.length() - chs.length);
                            }

                        }
//==============================================================================                            
                    } else {
                        if (cbuf_inx1 > 0) {
                            cbuf_len = cbuf_inx1 - 1;
                            cbuf_inx2 = 1;
                        }
                        index = 0;
                        cbuf_inx1 = 0;
                    }
                }
                fr.close();
                return "";
            } catch (IOException ex) {
                Logger.getLogger(Lib.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Lib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return str;
    }

    /**
     * 從檔案中依分隔符號列表取得類別名稱清單。
     *
     * @param fileName 目標檔案路徑
     * @param sepstr   分隔符號對陣列（sepstr[0]/[1] 為開始標記，sepstr[2]/[3] 為結束標記）
     * @return 符合條件的類別名稱列表
     */
    public static List<String> getFileClassNames(String fileName, String[] sepstr) {
        List<String> lsClassName = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            String[] strA, strB;
            String className = "";
            int findInx = 0;
            String line;
            while (true) {
                line = reader.readLine();

                if (line == null) {
                    break;
                }
                if (line.contains(sepstr[0])) {
                    strA = line.split(sepstr[0]);
                    if (strA.length != 2) {
                        continue;
                    }
                    strB = strA[1].split(sepstr[1]);
                    if (strB.length != 2) {
                        continue;
                    }
                    className = strB[0];
                    findInx = 1;
                    continue;
                }
                if (findInx == 0) {
                    continue;
                }
                if (line.contains(sepstr[2])) {
                    findInx = 0;
                    strA = line.split(sepstr[2]);
                    if (strA.length != 2) {
                        continue;
                    }
                    strB = strA[1].split(sepstr[3]);
                    if (strB.length != 2) {
                        continue;
                    }
                    if (!className.equals(strB[0])) {
                        continue;
                    }
                    lsClassName.add(className);
                    System.out.println(className);
                }
            }
            reader.close();
            return lsClassName;
        } catch (IOException e) {
            return lsClassName;
        }

    }

    /**
     * 從檔案載入模型類別庫至静態變數 lsClassName 與 lsClassData。
     *
     * @param fileName 目標檔案路徑
     * @param sepstr   分隔符號對陣列
     */
    public static void getModelClassLib(String fileName, String[] sepstr) {
        BufferedReader reader;
        Lib.lsClassName = new ArrayList<>();
        Lib.lsClassData = new ArrayList<>();

        try {
            File f = new File(fileName);
            InputStreamReader read = new InputStreamReader(new FileInputStream(f), "UTF-8");
            reader = new BufferedReader(read);
            //reader = new BufferedReader(new FileReader(fileName));
            String[] strA, strB;
            String className = "";
            String classData = "";

            int findInx = 0;
            String line;
            while (true) {
                line = reader.readLine();

                if (line == null) {
                    break;
                }
                classData += "\n" + line;
                if (line.contains(sepstr[0])) {
                    strA = line.split(sepstr[0]);
                    if (strA.length != 2) {
                        continue;
                    }
                    strB = strA[1].split(sepstr[1]);
                    if (strB.length != 2) {
                        continue;
                    }
                    className = strB[0];
                    classData = line;
                    findInx = 1;
                    continue;
                }
                if (findInx == 0) {
                    continue;
                }
                if (line.contains(sepstr[2])) {
                    findInx = 0;
                    strA = line.split(sepstr[2]);
                    if (strA.length != 2) {
                        continue;
                    }
                    strB = strA[1].split(sepstr[3]);
                    if (strB.length != 2) {
                        continue;
                    }
                    if (!className.equals(strB[0])) {
                        continue;
                    }
                    Lib.lsClassName.add(className);
                    Lib.lsClassData.add(classData);
                }
            }
            reader.close();
        } catch (IOException e) {
        }
    }

    /**
     * 將當前模型類別庫寫入檔案，並以 addString 取代 className 對應的資料。
     *
     * @param fileName  目標檔案路徑
     * @param className 要替換的類別名稱
     * @param addString 新的類別內容字串
     */
    public static void saveModelClassLib(String fileName, String className, String addString) {
        FileOutputStream outfile = null;
        String source = "";
        System.out.println(fileName);

        for (int i = 0; i < Lib.lsClassData.size(); i++) {
            System.out.println(Lib.lsClassName.get(i));
            if (!Lib.lsClassName.get(i).equals(className)) {
                source += "\n";
                source += Lib.lsClassData.get(i);
            }
        }
        source += "\n";
        source += addString;
        try {
            outfile = new FileOutputStream(fileName);
            outfile.write(source.getBytes("utf-8"));
            outfile.close();
        } catch (IOException e) {

        }
    }

    /**
     * 從當前模型類別庫中移除指定類別並寫入檔案。
     *
     * @param fileName  目標檔案路徑
     * @param className 要移除的類別名稱
     */
    public static void removeModelClassLib(String fileName, String className) {
        FileOutputStream outfile = null;
        String source = "";
        for (int i = 0; i < Lib.lsClassName.size(); i++) {
            if (!Lib.lsClassName.get(i).equals(className)) {
                source += Lib.lsClassData.get(i);
            }
        }
        try {
            outfile = new FileOutputStream(fileName);
            outfile.write(source.getBytes("utf-8"));
            outfile.close();
        } catch (IOException e) {
        }
    }

    /**
     * 將 JSON 字串轉換為 JavaScript 物件變數展開格式字串（透過 JsData 物件）。
     *
     * @param jsonStr 輸入 JSON 字串
     * @return 轉換後的字串
     */
    public static String json2Obj(String jsonStr) {
        JsData jd = new JsData(jsonStr);
        jd.transObj();
        return jd.outStr;
    }

    /**
     * 將 JSONArray 轉換為字串陣列。
     *
     * @param array 輸入 JSONArray
     * @return 對應的字串陣列， array 為 null 時回傳 null
     */
    public static String[] toStringArray(JSONArray array) {
        if (array == null) {
            return null;
        }

        String[] arr = new String[array.length()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = array.optString(i);
        }
        return arr;
    }

    /**
     * 將 JSONArray 轉換為字串清單 ArrayList。
     *
     * @param array 輸入 JSONArray
     * @return 對應的 ArrayList， array 為 null 時回傳 null
     */
    public static ArrayList<String> toStringList(JSONArray array) {
        if (array == null) {
            return null;
        }
        ArrayList<String> strList = new ArrayList<String>();
        for (int i = 0; i < array.length(); i++) {
            strList.add(array.optString(i));
        }
        return strList;
    }

    /**
     * 讓當前執行緒休眠指定毫秒，忖略中斷例外。
     *
     * @param ms 休眠毫秒數
     */
    public static void thSleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ex) {

        }
    }

    /**
     * 將字串清單轉換為 JSON 陣列格式字串（如 ["a","b","c"]）。
     *
     * @param alString 輸入字串清單
     * @return JSON 陣列格式字串
     */
    public static String stringListToString(ArrayList<String> alString) {
        String outStr = "[";
        for (int i = 0; i < alString.size(); i++) {
            if (i != 0) {
                outStr += ",";
            }
            outStr += "\"";
            outStr += alString.get(i);
            outStr += "\"";
        }
        outStr += "]";
        return outStr;

    }
    /**
     * 偵測目前作業系統並設定 GB.osInx 與 GB.osName。
     *
     * @return 作業系統索引：0=win, 1=linux, 2=mac, 3=sunos, -1=unknown
     */
    static int getOs() {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win")) {
            GB.osInx=0;
            GB.osName="win";    
            return 0;
        }
        if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
            GB.osInx=1;
            GB.osName="linux";
            return 1;
        }
        if (OS.contains("mac")) {
            GB.osInx=2;
            GB.osName="mac";
            return 2;
        }
        if (OS.contains("sunos")) {
            GB.osInx=3;
            GB.osName="sunos";
            return 3;
        }
        GB.osInx=-1;
        GB.osName="unknown";
        return -1;

    }
    /**
     * 對 hop 資料進行解密位移運算。
     *
     * @param hop    要解密的資料陣列
     * @param enckey 加密金鑰陣列
     */
    static void dechop(byte[] hop, byte[] enckey) {
        int i, j, ibuf, ibuf1;
        for (i = 0; i < 11; i++) {
            for (j = 0; j < 48; j++) {
                ibuf = 1;
                if ((hop[3] & 0x08) != 0) {
                    ibuf = 0x10;
                }
                if ((hop[2] & 0x01) != 0) {
                    ibuf <<= 2;
                }

                if ((hop[1] & 0x01) != 0) {
                    ibuf <<= 1;
                }
                if ((hop[4] & 0x40) != 0) {
                    ibuf1 = 0x5c;
                    if ((hop[4] & 0x02) != 0) {
                        ibuf1 = 0x3a;
                    }
                } else {
                    ibuf1 = 0x2e;
                    if ((hop[4] & 0x02) != 0) {
                        ibuf1 = 0x74;
                    }
                }
                ibuf = ibuf & ibuf1;
                if (ibuf != 0) {
                    ibuf = 0x80;
                }
                ibuf ^= hop[2];
                ibuf ^= hop[4];
                ibuf ^= enckey[1];
                ibuf = ibuf << 1;
                hop[1] = (byte) (hop[1] << 1);
                hop[2] = (byte) (hop[2] << 1);
                hop[3] = (byte) (hop[3] << 1);
                hop[4] = (byte) (hop[4] << 1);
                if ((ibuf & 0x100) != 0) {
                    hop[1]++;
                }
                if ((hop[1] & 0x100) != 0) {
                    hop[2]++;
                }
                if ((hop[2] & 0x100) != 0) {
                    hop[3]++;
                }
                if ((hop[3] & 0x100) != 0) {
                    hop[4]++;
                }
                enckey[0] <<= 1;
                enckey[1] <<= 1;
                enckey[2] <<= 1;
                enckey[3] <<= 1;
                enckey[4] <<= 1;
                enckey[5] <<= 1;
                enckey[6] <<= 1;
                enckey[7] <<= 1;
                if ((enckey[7] & 0x100) != 0) {
                    enckey[0]++;
                }
                if ((enckey[0] & 0x100) != 0) {
                    enckey[1]++;
                }
                if ((enckey[1] & 0x100) != 0) {
                    enckey[2]++;
                }
                if ((enckey[2] & 0x100) != 0) {
                    enckey[3]++;
                }
                if ((enckey[3] & 0x100) != 0) {
                    enckey[4]++;
                }
                if ((enckey[4] & 0x100) != 0) {
                    enckey[5]++;
                }
                if ((enckey[5] & 0x100) != 0) {
                    enckey[6]++;
                }
                if ((enckey[6] & 0x100) != 0) {
                    enckey[7]++;
                }
            }
        }
    }
    /**
     * 從網路介面設定檔讀取包含指定字丣的行，回傳對應行第二欄位的內容。
     *
     * @param cmpstr 要比對的字丣
     * @return 第二欄位的內容，找不到時回傳 null
     */
    static String rdInterfaces(String cmpstr) {
        String fnameInterfaces = GB.interfacesPath;
        File f = new File(fnameInterfaces);
        if (f.exists() && !f.isDirectory()) {
            FileReader fr;
            BufferedReader br;
            String[] fields;
            String tmp;
            String str;

            try {
                fr = new FileReader(fnameInterfaces);
                br = new BufferedReader(fr);
                while ((tmp = br.readLine()) != null) {
                    if (tmp.contains(cmpstr)) {
                        str = tmp.trim();
                        fields = str.split("[ ]+");
                        br.close();
                        return fields[1];
                    }
                }
                fr.close();
                br.close();
                return null;
            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
            }

        } else {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (Exception ex) {
            }
        }
        return null;

    }

    /**
     * 寫入静態 IP 網路介面設定到設定檔。
     *
     * @param netName    網路介面名稱（如 eth0）
     * @param ipStr      IP 位址字串
     * @param maskStr    子網遮罩字串
     * @param gatewayStr 預設閘道字串
     * @return 1 表示成功，0 表示失敗
     */
    public static int wrInterfaces(String netName, String ipStr, String maskStr, String gatewayStr) {
        String fname;
        String bstr;
        fname = GB.interfacesPath;
        try {
            FileWriter fw = new FileWriter(fname);
            fw.write("\nauto lo");
            fw.write("\niface lo inet loopback");
            fw.write("\n");
            fw.write("\nauto " + netName);
            fw.write("\niface " + netName + " inet static");
            bstr = "\naddress " + ipStr;
            fw.write(bstr);
            bstr = "\nnetmask " + maskStr;
            fw.write(bstr);
            bstr = "\ngateway " + gatewayStr;
            fw.write(bstr);
            fw.flush();
            fw.close();
            return 1;
        } catch (FileNotFoundException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return 0;
    }
    /**
     * 在字串中搜尋起始與結束字串之間的內容，結果存入靜態變數 retstr。
     *
     * @param str 輸入字串
     * @param st  起始字丣
     * @param end 結束字丣
     * @return 1 表示成功，-1 表示找不到
     */
    static int search(String str, String st, String end) {
        int sti, endi;
        sti = str.indexOf(st);
        if (sti < 0) {
            return -1;
        }
        endi = str.indexOf(end, sti + st.length());
        if (endi < 0) {
            return -1;
        }
        retstr = str.substring(sti + st.length(), endi);
        return 1;
    }
    /**
     * 在字串中搜尋起始與結束字串之間的內容，找不到結束字丣時取到字串結尾。
     *
     * @param str 輸入字串
     * @param st  起始字丣
     * @param end 結束字丣
     * @return 1 表示成功，-1 表示找不到起始字丣
     */
    static int searchEnd(String str, String st, String end) {
        int sti, endi;
        sti = str.indexOf(st);
        if (sti < 0) {
            return -1;
        }
        endi = str.indexOf(end, sti + st.length());
        if (endi < 0) {
            endi = str.length();
        }
        retstr = str.substring(sti + st.length(), endi);
        return 1;
    }
    /**
     * 讀取目錄中符合比對名稱規則的檔案名稱列表。
     *
     * @param initDir      起始目錄路徑
     * @param compareNames 比對名稱陣列（支持萬用字元 *）
     * @return 符合條件的檔案名稱列表
     */
    public static ArrayList<String> readFileNames(String initDir, String[] compareNames) {
        String fileName;
        ArrayList<String> fileNameList = new ArrayList<String>();
        File folder = new File(initDir);
        if (folder.exists() && folder.isDirectory()) {
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    fileName = listOfFiles[i].getName();
                    for (int j = 0; j < compareNames.length; j++) {
                        if (compareString(fileName, compareNames[j].trim()) == 1) {
                            fileNameList.add(fileName);
                            //System.out.println("File " + fileName);
                            break;
                        }
                    }
                    /*
                    if(extNames.length==0){
                        fileNameList.add("./" + initDir + "/" + fileName);
                        continue;
                    }
                    strA = fileName.split("\\.");
                    if (strA.length < 2) {
                        continue;
                    }
                    int j;
                    for(j=0;j<extNames.length;j++){
                        if(strA[strA.length - 1].equals(extNames[i]))
                            break;
                        if(strA[strA.length - 1].equals(extNames[i]))
                            break;
                        if(strA[strA.length - 1].equals(extNames[i]))
                            break;
                    }
                    if(j==extNames.length)
                        continue;
                    fileNameList.add("./" + initDir + "/" + fileName);
                    //System.out.println("File " + fileName);
                     */
                } else if (listOfFiles[i].isDirectory()) {
                    //System.out.println("Directory " + listOfFiles[i].getName());
                }
            }
        }
        return fileNameList;
    }

    //**find the string from between given string st and end
    /**
     * Find the string between given string stStr and endStr
     *
     * @param inStr The input string.
     * @param stStr The first match string. if is null from position 0.
     * @param endStr The end match string. if is null from position end.
     * @return if doesn't find the stStr or endStr return null, else return the
     * string between stStr and endStr.
     */
    static String getStrBetween(String inStr, String stStr, String endStr) {
        int sti, endi;
        if (stStr == null) {
            sti = 0;
        } else {
            sti = inStr.indexOf(stStr);
            if (sti < 0) {
                return null;
            }
        }
        if (endStr == null) {
            endi = inStr.length();
        } else {
            endi = inStr.indexOf(endStr, sti + stStr.length());
            if (endi < 0) {
                return null;
            }
        }
        retstr = inStr.substring(sti + stStr.length(), endi);
        return retstr;
    }

    /**
     * 在字串的指定起始與結束位置之間插入新字串。
     *
     * @param inStr  原始字串
     * @param insStr 要插入的字串
     * @param st     起始位置索引
     * @param end    結束位置索引
     * @return 插入後的字串
     */
    static String insertStrBetween(String inStr, String insStr, int st, int end) {
        String firstStr = inStr.substring(0, st);
        String endStr = inStr.substring(end, inStr.length());
        return firstStr + insStr + endStr;
    }

    /**
     * 讀取設定檔並解析 [key]&lt;value&gt; 格式到 HashMap。
     *
     * @param fileFullName 設定檔完整路徑
     * @param hmap         存放解析結果的 HashMap
     * @return 讀取成功回傳 true，失敗回傳 false
     */
    static boolean readSetdataFileToPara(String fileFullName, HashMap<String, String> hmap) {
        FileReader reader;
        try {
            reader = new FileReader(fileFullName);
            BufferedReader br = new BufferedReader(reader);
            String line;
            String paraN;
            String paraV;
            String str;
            while ((line = br.readLine()) != null) {
                str = line.trim();
                if (str.length() == 0) {
                    continue;
                }
                if (str.charAt(0) == '#') {
                    continue;
                }
                paraN = Lib.getStrBetween(str, "[", "]");
                if (paraN == null) {
                    continue;
                }
                paraN = paraN.trim();
                paraV = Lib.getStrBetween(str, "<", ">");
                if (paraV == null) {
                    continue;
                }
                hmap.put(paraN, paraV);
            }
            br.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 支援萬用字元 * 的字串比對函式。
     *
     * @param orgStr 原始字串
     * @param cmpStr 比對字串（可含萬用字元 *）
     * @return 1 表示比對成功，0 表示不符合
     */
    public static int compareString(String orgStr, String cmpStr) {
        int olen = orgStr.length();
        int clen = cmpStr.length();
        int i,  ibuf;
        String sbuf, tbuf;
        ibuf = cmpStr.indexOf('*');
        if (ibuf == -1) {
            if (orgStr.equals(cmpStr)) {
                return 1;
            }
            return 0;
        }
        if (ibuf == 0) {    //first *
            if (clen == 1) //cmpStr="*"
            {
                return 1;
            }
            if (cmpStr.charAt(clen - 1) == '*') {     //comStr="*xxx*"
                sbuf = cmpStr.substring(1, clen - 1);
                if (orgStr.indexOf(sbuf) == -1) {
                    return 0;
                }
                return 1;
            }
            sbuf = cmpStr.substring(1);

            int slen = sbuf.length();
            for (i = 0; i < slen; i++) {
                if (sbuf.charAt(slen - 1 - i) != orgStr.charAt(olen - 1 - i)) {
                    return 0;
                }
            }
            return 1;
        }
        if (ibuf == clen - 1) {   //end *
            sbuf = cmpStr.substring(0, clen - 1);
            ibuf = orgStr.indexOf(sbuf);
            if (ibuf != 0) {
                return 0;
            }
            return 1;
        }
        sbuf = cmpStr.substring(0, ibuf);
        tbuf = cmpStr.substring(ibuf + 1, clen);
        ibuf = orgStr.indexOf(sbuf);
        if (ibuf != 0) {
            return 0;
        }
        int tlen = tbuf.length();
        for (i = 0; i < tlen; i++) {
            if (tbuf.charAt(tlen - 1 - i) != orgStr.charAt(olen - 1 - i)) {
                return 0;
            }
        }
        if (sbuf.length() + tbuf.length() > olen) {
            return 0;
        }
        return 1;
    }

    /**
     * 寫入簡化版静態 IP 網路介面設定（固定介面名稱 eth0）。
     *
     * @param ip      IP 位址字串
     * @param mask    子網遮罩字串
     * @param gateway 預設閘道字串
     * @return 1 表示成功，0 表示失敗
     */
    static int wrInterfaces(String ip, String mask, String gateway) {
        String fname;
        String bstr;
        fname = GB.interfacesPath;
        int debug_i = 0;
        if (debug_i == 1) {
            return 0;
        }

        try {
            FileWriter fw = new FileWriter(fname);
            fw.write("auto lo\n");
            fw.write("iface lo inet loopback\n");
            fw.write("\n");
            fw.write("auto eth0\n");
            //if(GB.ip_type==1)
            //  fw.write("iface eth0 inet dhcp\n");
            //else
            fw.write("iface eth0 inet static\n");

            bstr = "address " + ip + "\n";
            fw.write(bstr);
            bstr = "netmask " + mask + "\n";
            fw.write(bstr);
            bstr = "gateway " + gateway + "\n";
            fw.write(bstr);
            fw.flush();
            fw.close();
            return 1;
        } catch (FileNotFoundException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return 0;
    }


    /**
     * 寫入 NTP 伺服器設定到設定檔。
     *
     * @param ip NTP 伺服器 IP 位址字串
     * @return 1 表示成功，0 表示失敗
     */
    static int wrNtp(String ip) {
        String fname;
        fname = GB.ntpConfPathName;
        System.out.println("ntpIp "+ip+ " ==>"+fname);

        try {
            FileWriter fw = new FileWriter(fname);
            fw.write("[Time]\n");
            fw.write("NTP="+ip+"\n");
            fw.flush();
            fw.close();
            return 1;
        } catch (FileNotFoundException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return 0;
    }



}

class JsData {

    String inStr = "";
    String outStr = "";
    int inx = 0;
    int err_i = 0;
    int len;

    JsData(String str) {
        inStr = str;
        len = inStr.length();
    }

    public void transObj() {
        char ch;
        for (; inx < len; inx++) {
            ch = inStr.charAt(inx);
            outStr += ch;
            if (ch == '{') {
                inx++;
                tobj();
            }
        }
    }

    public void tobj() {
        char ch;
        int mm_i = 0;
        int array_i = 0;
        int pp_i = 0;
        for (; inx < len; inx++) {
            ch = inStr.charAt(inx);
            if (mm_i == 0) {
                if (ch == '\"') {
                    continue;
                }
                if (ch == ':') {
                    mm_i = 1;
                }
                outStr += ch;
                continue;
            } else {
                outStr += ch;
                if (ch == '[') {
                    array_i = 1;
                    continue;
                }
                if (ch == '\"') {
                    if (pp_i == 0) {
                        pp_i = 1;
                    } else {
                        pp_i = 0;
                    }
                    continue;
                }

                if (array_i == 0) {
                    if (ch == ',') {
                        if (pp_i == 0) {
                            outStr += '\n';
                            mm_i = 0;
                        }
                        continue;
                    }
                    if (ch == '{') {
                        inx++;
                        tobj();
                        continue;
                    }
                } else {
                    if (ch == ']') {
                        array_i = 0;
                        continue;
                    }
                    if (ch == '{') {
                        inx++;
                        tobj();
                        continue;
                    }
                    if (ch == '}') {
                        return;
                    }

                }
            }
        }
    }
}

abstract class MapCbk {

    public abstract String prg(String sendJid, Map<String, String> map);
}

abstract class AbsRxBytes {

    public abstract void rx(byte[] bytes, int len);
}

interface StrCallback {

    public String prg(String vstr);
}

interface BytesCallback {

    public String prg(byte[] bytes, int len);
}

interface ObjCallback {

    public String prg(Object vobj);
}

interface StrObjCallback {

    public String prg(String str, Object vobj);
}

class KvJson {

    String jstr = "";
    int keyCnt = 0;

    KvJson() {

    }

    void jStart() {
        keyCnt = 0;
        jstr = "{";
    }

    String jEnd() {
        keyCnt = 0;
        jstr += "}";
        return jstr;
    }

    void jadd(String key, int ii) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += ii;
        keyCnt++;
    }

    void jadd(String key, long ii) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += ii;
        keyCnt++;
    }

    void jadd(String key, short ii) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += ii;
        keyCnt++;
    }

    void jadd(String key, byte ii) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += ii;
        keyCnt++;
    }

    void jadd(String key, float ff) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += ff;
        keyCnt++;
    }

    void jadd(String key, String ss) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += ss;
        keyCnt++;
    }

    void jadd(String key, long[] ia) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        if (ia != null) {
            for (int i = 0; i < ia.length; i++) {
                if (i != 0) {
                    jstr += ",";
                }
                jstr += ia[i];
            }
        }
        jstr += "]";
        keyCnt++;
    }

    void jadd(String key, int[] ia) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        if (ia != null) {
            for (int i = 0; i < ia.length; i++) {
                if (i != 0) {
                    jstr += ",";
                }
                jstr += ia[i];
            }
        }
        jstr += "]";
        keyCnt++;
    }

    void jadd(String key, int[] ia, int len) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        int iaLen = len;
        if (ia != null) {
            for (int i = 0; i < iaLen; i++) {
                if (i != 0) {
                    jstr += ",";
                }
                jstr += ia[i];
            }
        }
        jstr += "]";
        keyCnt++;
    }

    void jadd(String key, short[] ia) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        for (int i = 0; i < ia.length; i++) {
            if (i != 0) {
                jstr += ",";
            }
            jstr += ia[i];
        }
        jstr += "]";
        keyCnt++;
    }

    void jadd(String key, byte[] ia) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        for (int i = 0; i < ia.length; i++) {
            if (i != 0) {
                jstr += ",";
            }
            jstr += ia[i] & 255;
        }
        jstr += "]";
        keyCnt++;
    }

    void jadd(String key, float[] fa) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        for (int i = 0; i < fa.length; i++) {
            if (i != 0) {
                jstr += ",";
            }
            jstr += fa[i];
        }
        jstr += "]";
        keyCnt++;
    }

    void jadd(String key, String[] sa) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        for (int i = 0; i < sa.length; i++) {
            if (i != 0) {
                jstr += ",";
            }
            jstr += sa[i];
        }
        jstr += "]";
        keyCnt++;
    }

    void jadd(String key, int[][] iaa) {
        if (keyCnt != 0) {
            jstr += ",";
        }
        jstr += "\"" + key + "\":";
        jstr += "[";
        for (int i = 0; i < iaa.length; i++) {
            if (i != 0) {
                jstr += ",";
            }
            jstr += "[";
            for (int j = 0; j < iaa[i].length; j++) {
                if (j != 0) {
                    jstr += ",";
                }
                jstr += iaa[i][j];
            }
            jstr += "]";
        }
        jstr += "]";
        keyCnt++;
    }

    static String objToJson(Object inst) {
        String ss;
        byte bb;
        int ii;
        long ll;
        float ff;
        double dd;
        String[] sa;
        byte[] ba;
        int[] ia;
        long[] la;
        float[] fa;
        double[] da;
        String jstr;
        try {
            Class<?> aClassHandle = inst.getClass();
            Field[] fields = aClassHandle.getDeclaredFields();
            String jsonStr = "{";
            for (int i = 0; i < fields.length; i++) {
                Object value = fields[i].get(inst);
                if (value == null) {
                    continue;
                }
                String keyName = "";
                if (i != 0) {
                    keyName += ",";
                }
                keyName += "\"" + fields[i].getName() + "\": ";
                if (value instanceof String) {
                    ss = (String) value;
                    jsonStr += keyName + "\"" + ss.replace("\n", "\\n") + "\"";
                } else if (value instanceof Byte) {
                    bb = (Byte) value;
                    jsonStr += keyName + bb;
                } else if (value instanceof Integer) {
                    ii = (Integer) value;
                    jsonStr += keyName + ii;
                } else if (value instanceof Long) {
                    ll = (Long) value;
                    jsonStr += keyName + ll;
                } else if (value instanceof Float) {
                    ff = (Float) value;
                    jsonStr += keyName + ff;
                } else if (value instanceof Double) {
                    dd = (Double) value;
                    jsonStr += keyName + dd;
                } else if (value instanceof String[]) {
                    sa = (String[]) value;
                    jstr = "[";
                    for (int j = 0; j < sa.length; j++) {
                        if (j != 0) {
                            jstr += ",";
                        }
                        jstr += "\"" + sa[j].replace("\n", "\\n") + "\"";
                    }
                    jstr += "]";
                    jsonStr += keyName + jstr;
                } else if (value instanceof byte[]) {
                    ba = (byte[]) value;
                    jsonStr += keyName + Arrays.toString(ba);
                } else if (value instanceof int[]) {
                    ia = (int[]) value;
                    jsonStr += keyName + Arrays.toString(ia);
                } else if (value instanceof long[]) {
                    la = (long[]) value;
                    jsonStr += keyName + Arrays.toString(la);
                } else if (value instanceof float[]) {
                    fa = (float[]) value;
                    jsonStr += keyName + Arrays.toString(fa);
                } else if (value instanceof double[]) {
                    da = (double[]) value;
                    jsonStr += keyName + Arrays.toString(da);
                } else {
                    Class<?> vClassHandle = value.getClass();
                    String classHandleName = vClassHandle.getName();
                    if (classHandleName.contains("HashMap")) {
                        @SuppressWarnings("unchecked")
                        HashMap<String, Object> map = (HashMap<String, Object>) value;
                        jsonStr += keyName + "{";
                        int kinx = 0;
                        for (String key : map.keySet()) {
                            if (kinx != 0) {
                                jsonStr += ",";
                            }
                            kinx++;
                            jsonStr += "\"" + key + "\": ";
                            String tstr = KvJson.objToJson(map.get(key));
                            jsonStr += tstr;
                        }
                        jsonStr += "}";

                    }
                    if (classHandleName.contains("kevin")) {
                        if (classHandleName.contains("[L")) {
                            Object[] objA = (Object[]) value;
                            jsonStr += keyName + "[";
                            for (int j = 0; j < objA.length; j++) {
                                if (j != 0) {
                                    jsonStr += ",";
                                }
                                String tstr = KvJson.objToJson(objA[j]);
                                jsonStr += tstr;
                            }
                            jsonStr += "]";

                        } else {
                            String subJsonStr = KvJson.objToJson(value);
                            jsonStr += keyName + subJsonStr;
                        }
                    }
                }
            }
            jsonStr += "}";
            return jsonStr;
        } catch (Exception ex) {
            Logger.getLogger(KvJson.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}

class ConnectCla {

    String name;
    int time = 0;
    int timeTh = 0;

    public ConnectCla(String _name, int _timeTh) {
        time = 0;
        name = _name;
        timeTh = _timeTh;
    }
}
