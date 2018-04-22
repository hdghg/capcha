package com.github.hdghg.capcha.controller;

import com.github.hdghg.capcha.domain.Tile;
import com.github.hdghg.capcha.repository.TileReactiveRepository;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Controller;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides controller to upload new images with tags
 */
@Controller
@RequestMapping
public class UploadController {

    private final TileReactiveRepository tileReactiveRepository;

    public UploadController(TileReactiveRepository tileReactiveRepository) {
        this.tileReactiveRepository = tileReactiveRepository;
    }

    /**
     * Initial upload page
     *
     * @return Map of uploadPage attributes
     */
    @RequestMapping("uploadPage")
    private Mono<Map<String, ?>> viewUploadPage() {
        return tileReactiveRepository.findTop5ByOrderByIdDesc()
                .map(t -> t.image)
                .map(Base64Utils::encodeToString)
                .collect(Collectors.toList())
                .map(l -> Collections.singletonMap("recent", l));
    }

    /**
     * Controller that accepts POST queries with multipart form data
     *
     * @param filePart File being uploaded
     * @param tags     Tags for given file
     * @return Redirection to {@link UploadController#viewUploadPage()}
     */
    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> doUpload(@RequestPart("file") FilePart filePart,
                                 @RequestPart("tags") FormFieldPart tags) {
        Set<String> tagSet = Flux.just(tags.value().split(","))
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet())
                .block();
        return filePart.content()
                .collect(ByteArrayOutputStream::new, (acc, buf) ->
                        acc.write(buf.asByteBuffer().array(), 0, buf.readableByteCount()))
                .map(baos -> new Tile(baos.toByteArray(), tagSet))
                .flatMap(tileReactiveRepository::save)
                .map(unbound -> "redirect:/uploadPage");
    }
}
