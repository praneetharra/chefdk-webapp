package com.cerner.hdxts.chefdk.webapp;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cerner.hdxts.chefdk.webapp.storage.StorageFileNotFoundException;
import com.cerner.hdxts.chefdk.webapp.storage.StorageService;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(final StorageService storageService) {
        this.storageService = storageService;
    }

    @RequestMapping(value = "/existingFiles", method = RequestMethod.GET, consumes="application/json")
	public ResponseEntity<?> response() {
    	
    	List<String> files = storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList());
                
                
    	return new ResponseEntity<List>(files, HttpStatus.OK) ;
    }
    
    
    @GetMapping("/")
    public String listUploadedFiles(final Model model) throws IOException {

        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable final String filename) {

        final Resource file = storageService.loadAuthAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/auth")
    public String handleAuthFileUpload(@RequestParam("file") final MultipartFile file,
            final RedirectAttributes redirectAttributes) {

        storageService.storeAuthFile(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @PostMapping("/secrets")
    public ResponseEntity<?> handleSecretsFileUpload(@RequestParam("file") final MultipartFile file,
            final RedirectAttributes redirectAttributes) {

        storageService.storeSecretFile(file);
       
        return new ResponseEntity<String>("success: submitted file " + file.getOriginalFilename().toString(), HttpStatus.OK);
    }

    @PostMapping("/uploadJSON")
    public ResponseEntity<?> handleJSONFileUpload(@RequestParam("file") final MultipartFile file,
            final RedirectAttributes redirectAttributes) {

        storageService.storeJSONFile(file);
     
        return new ResponseEntity<String>(file.getOriginalFilename().toString(), HttpStatus.OK);
    }
    
    static String extractPostRequestBody(HttpServletRequest request) throws IOException {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
        return "";
    }

    @RequestMapping(value = "/executeScript", method = RequestMethod.POST, consumes="application/json", produces = "application/json")
    public ResponseEntity<?> executeScript(HttpServletRequest req) throws IOException {

        final StringBuffer sb = new StringBuffer();
        try {
            // Run the process
           
            String str = extractPostRequestBody(req);
            String secret = str.substring(str.indexOf("name_of_secret")+17, str.indexOf("name_of_json")-3); ;             
            String json = str.substring(str.indexOf("name_of_json")+15, str.indexOf(".json"));

        	
        	Runtime.getRuntime().exec("knife data bag create databag " + json + " -z");
            Runtime.getRuntime().exec("knife data bag from file databag /etc/json/" + json + ".json --secret-file /etc/secrets/" + secret + " -z");
        
            
        	final Process p = Runtime.getRuntime().exec("knife data bag show databag " + json + " -F json -z");
        	
            // Get the input stream
            final InputStream is = p.getInputStream();

            // Read script execution results
            int i = 0;
            while ((i = is.read()) != -1) {
                sb.append((char) i);
            }
            
                   
            System.out.println(sb.toString());

        } catch (final IOException e) {
            e.printStackTrace();
        }
         	
    	System.out.println("recieved request with data: " + extractPostRequestBody(req));
    	
    	
    	if (sb.toString().contains("{")) {
        	String responseJson = sb.toString().substring(sb.toString().indexOf("{"));
            return new ResponseEntity<String>(responseJson, HttpStatus.OK);
    	} else { 
    		executeScript(req);
    	}
    	
        return new ResponseEntity<String>("Something has failed, try again", HttpStatus.CONFLICT);

    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(final StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
