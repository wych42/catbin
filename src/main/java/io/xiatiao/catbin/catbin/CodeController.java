package io.xiatiao.catbin.catbin;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@Controller
public class CodeController {
    @Autowired
    private CustomCodeRepository codeRepository;

    @GetMapping(value="/")
    public String index(WebRequest request, Model model){
        String url = ((ServletWebRequest) request).getRequest().getRequestURL().toString();
        String protocol = request.getHeader("x-forwarded-proto");
        if (Strings.isNullOrEmpty(protocol)) {
            protocol = request.getHeader("x-forwarded-protocol");
        }
        if (Strings.isNullOrEmpty(protocol)) {
            String forwardedSsl = request.getHeader("x-forwarded-ssl");
            if (!Strings.isNullOrEmpty(forwardedSsl) && forwardedSsl.equals("on")) {
                protocol = "https";
            }
        }
        if (Strings.isNullOrEmpty(protocol)) {
            protocol = "http";
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        url =  builder.scheme(protocol).toUriString();
        model.addAttribute("url", url);
        return "index";
    }

    @PostMapping(path = "/")
    @ResponseBody
    public ResponseEntity<String> index(@RequestParam(name="cat") String code) {
        if (code.length() < 23 || code.length() > 64 * 1024) {
            return new ResponseEntity<>(
                    "the code snippet must be longer than 'print(\"Hello, world!\")' or shorter than 64 KiB.",
                    HttpStatus.PAYLOAD_TOO_LARGE);
        }
        String digest = Hashing.sha256().hashString(code, StandardCharsets.UTF_8).toString();
        Code row = codeRepository.findByDigest(digest);
        if (row != null) {
            return new ResponseEntity<>(row.getId().toString(), HttpStatus.OK);
        }
        Code c = new Code();
        c.setContent(code);
        c.setDigest(digest);
        Code sc = codeRepository.save(c);
        return new ResponseEntity<>(sc.getId().toString(), HttpStatus.OK);
    }
}
