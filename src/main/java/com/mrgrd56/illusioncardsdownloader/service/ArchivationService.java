package com.mrgrd56.illusioncardsdownloader.service;

import com.mrgrd56.illusioncardsdownloader.model.NamedFile;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ArchivationService {
    public byte[] zipFiles(Collection<NamedFile> files) throws IOException {
        var outputStream = new ByteArrayOutputStream();
        var zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8);

        for (var file : files) {
            var entry = new ZipEntry(file.getName());
            entry.setSize(file.getContent().length);

            zipOutputStream.putNextEntry(entry);
            zipOutputStream.write(file.getContent());
            zipOutputStream.closeEntry();
        }

        zipOutputStream.close();
        return outputStream.toByteArray();
    }
}
