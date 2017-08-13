package org.owasp.webwolf;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for uploading a file
 */
@Controller
@Slf4j
public class FileServer {

    @Value("${webwolf.fileserver.location}")
    private String fileLocatation;

    @PostMapping(value = "/fileupload")
    @SneakyThrows
    public ModelAndView importFile(@RequestParam("file") MultipartFile myFile) {
        WebGoatUser user = (WebGoatUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        File destinationDir = new File(fileLocatation, user.getUsername());
        destinationDir.mkdirs();
        myFile.transferTo(new File(destinationDir, myFile.getOriginalFilename()));
        log.debug("File saved to {}", new File(destinationDir, myFile.getOriginalFilename()));

        ModelMap model = new ModelMap();
        model.addAttribute("uploadSuccess", "File uploaded successful");
        return new ModelAndView(
                new RedirectView("files", true),
                model
        );
    }

    @AllArgsConstructor
    @Getter
    private class UploadedFile {
        private final String name;
        private final String size;
        private final String link;
    }

    @GetMapping(value = "/files")
    public ModelAndView getFiles(HttpServletRequest request) {
        WebGoatUser user = (WebGoatUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = user.getUsername();
        File destinationDir = new File(fileLocatation, username);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("files");
        modelAndView.addObject("uploadSuccess", request.getParameter("uploadSuccess"));

        List<UploadedFile> uploadedFiles = new ArrayList<>();
        File[] files = destinationDir.listFiles(File::isFile);
        for (File file : files) {
            String size = FileUtils.byteCountToDisplaySize(file.length());
            String link = String.format("files/%s/%s", username, file.getName());
            uploadedFiles.add(new UploadedFile(file.getName(), size, link));
        }

        modelAndView.addObject("files", uploadedFiles);
        return modelAndView;
    }
}
