package io.xiatiao.catbin.catbin;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
public class CodeController {
    @Autowired
    private CustomCodeRepository codeRepository;

    private String getUrl(WebRequest request) {
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
        url = builder.scheme(protocol).toUriString();
        return url;
    }

    @GetMapping(value = "/")
    public String index(WebRequest request, Model model) {
        String url = getUrl(request);
        model.addAttribute("url", url);
        return "index";
    }

    private List<String> terminalUA = Arrays.asList("wget", "curl");

    private boolean isTerminal(String ua) {
        for (String tua : terminalUA) {
            if (ua.contains(tua)) {
                return true;
            }
        }
        return false;
    }

    @GetMapping(value = {"/{id:\\d+}", "/{id:\\d+}/{syntax:[a-z]+}"})
    public String index(WebRequest request, Model model,
                        HttpServletResponse response,
                        @PathVariable Integer id,
                        @PathVariable(required = false) String syntax) {
        Optional<Code> c = codeRepository.findById(id);
        if (c.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id.toString() + " not found");
        }
        String ua = request.getHeader("User-Agent");
        if (syntax == null || ua == null || isTerminal(ua)) {
            model.addAttribute("code", c.get().getContent());
            HttpHeaders rh = new HttpHeaders();
            response.addHeader("Content-Type", "text/plain; charset=utf-8");
            return "plaincode";
        }
        model.addAttribute("syntax", syntax);
        model.addAttribute("code", c.get().getContent());
        return "code";
    }

    @PostMapping(path = "/")
    @ResponseBody
    public ResponseEntity<String> index(WebRequest request, @RequestParam(name = "cat") String code) {
        if (code.length() < 23 || code.length() > 64 * 1024) {
            return new ResponseEntity<>(
                    "the code snippet must be longer than 'print(\"Hello, world!\")' or shorter than 64 KiB.",
                    HttpStatus.PAYLOAD_TOO_LARGE);
        }
        String url = getUrl(request);
        String digest = Hashing.sha256().hashString(code, StandardCharsets.UTF_8).toString();
        Code row = codeRepository.findByDigest(digest);
        if (row != null) {
            return new ResponseEntity<>(url + row.getId().toString(), HttpStatus.OK);
        }
        Code c = new Code();
        c.setContent(code);
        c.setDigest(digest);
        Code sc = codeRepository.save(c);
        return new ResponseEntity<>(url + sc.getId().toString(), HttpStatus.OK);
    }
}
