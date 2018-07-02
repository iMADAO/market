package cn.haizhi.market.main.handler.madao;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

@Controller
public class DownloadHandler {
//    @RequestMapping("/download")
//    public ResponseEntity<byte[]> testResponseEntity(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException{
//        byte[] body = null;
//        String fileName = "8719ceef5de32626c098ba66c2b9cfa0_86207900.apk";
//        ServletContext servletContext = session.getServletContext();
//        System.out.println(servletContext.getContextPath() + "/" + fileName);
//        InputStream in = servletContext.getResourceAsStream(fileName);
//        if(in==null)
//            System.out.println("IN: NULL");
//        body = new byte[in.available()];
//        in.read(body);
//
//        HttpHeaders headers = new HttpHeaders();
//		headers.add("Content-Disposition", "attachment;filename=" + fileName);
//        response.setHeader("Content-type", "application-download");
//        headers.setContentDispositionFormData("attachment", new String(fileName.getBytes("UTF-8"), "ISO8859-1"));
//        HttpStatus statusCode = HttpStatus.OK;
//
//        ResponseEntity<byte[]> responseEntity  = new ResponseEntity<byte[]>(body, headers, statusCode);
//        return responseEntity;
//    }
//

    @RequestMapping(value="/download", produces="application/vnd.android.package-archive")
      public ResponseEntity<byte[]>  testDownload(HttpServletRequest request,HttpServletResponse resp){
                String filename="app-debug.apk";
              ServletContext scontext=request.getServletContext();
              String path=scontext.getRealPath("/WEB-INF/"+filename);
                 System.out.println(path);
                File f=new File(path);
                InputStream in =null;
               ResponseEntity<byte[]> response=null ;
                try {
                        in = new FileInputStream(f);
                       byte[] b=new byte[in.available()];
                         in.read(b);
                         HttpHeaders headers = new HttpHeaders();
                        filename = new String(filename.getBytes(),"utf-8");
                         //MediaType mt = new MediaType("application/vnd.android.package-archive");
                         //headers.setContentType(mt);
                         //以下的类型必须要设置，要不不能在android机上正常下载的
                         headers.add("Content-Disposition", "attachment;filename="+filename);

                         //resp.setContentType("application/vnd.android.package-archive");
                         HttpStatus statusCode=HttpStatus.OK;
                         response = new ResponseEntity<byte[]>(b, headers, statusCode);

                     } catch (FileNotFoundException e) {
                         e.printStackTrace();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }finally{
                         try {
                                 in.close();
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }

                     }
                 return response;
             }
}
