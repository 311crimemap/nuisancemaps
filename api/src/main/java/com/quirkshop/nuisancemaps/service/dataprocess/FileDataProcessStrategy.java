package com.quirkshop.nuisancemaps.service.dataprocess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.DataJob;
import com.quirkshop.nuisancemaps.model.DataJobStatus;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.service.dataparser.DataParser;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class FileDataProcessStrategy implements DataProcessStrategy {

    private static final String FETCH_DATA_DIR = System.getenv("FETCH_DATA_DIR");
    private static final long DATA_DIR_MIN_FREE = Long.parseLong(System.getenv("DATA_DIR_MIN_FREE"));
    private final int ERROR_RATE = 5;

    @Autowired
    private FileStoreProvider fileStoreProvider;

    @Autowired
    private OkHttpClient client;

    @Autowired
    private DataJobRepository dataJobRepository;

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @Override
    public InputStream fetchData(DataJob dataJob) {
        InputStream inputStream = null;

        // only want initial source URL, no params or built URL
        String url = dataJob.getSourceURL();

        dataJob.setStatus(DataJobStatus.FETCH_START);
        dataJobRepository.save(dataJob);

        Request request = new Request.Builder().url(url).build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            inputStream = response.body().byteStream();
        } catch (IOException e) {
            System.err.println("Error fetchData: " + e.getMessage());
            dataJob.setStatus(DataJobStatus.FETCH_ERROR);
            dataJobRepository.save(dataJob);
            e.printStackTrace();
            return null;
        }

        dataJob.setStatus(DataJobStatus.FETCH_COMPLETE);
        dataJobRepository.save(dataJob);

        return inputStream;
    }

    @Override
    public void process(DataJob dataJob, InputStream inputStream, DataParser dataParser) {

        ParseCounter parseCounter = new ParseCounter();
        int bytesRead = 0;
        String filePath = null;

        dataJob.setStatus(DataJobStatus.PROCESS_START);
        dataJobRepository.save(dataJob);

        // WRITE

        try {
            if (!validDiskSpace()) {
                dataJob.setStatus(DataJobStatus.NO_SPACE_ERROR);
                dataJobRepository.save(dataJob);
                return;
            }

            String filename = dataJob.buildFilename();
            filePath = String.join("/", FETCH_DATA_DIR, filename);

            bytesRead = writeToFile(filePath, inputStream);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            dataJob.setStatus(DataJobStatus.BUILD_FILENAME_ERROR);
            dataJobRepository.save(dataJob);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            dataJob.setStatus(DataJobStatus.WRITE_FILE_ERROR);
            dataJobRepository.save(dataJob);
            return;
        }

        dataJob.setStatus(DataJobStatus.WRITE_COMPLETE);
        dataJobRepository.save(dataJob);
        log.info(String.format("Write Complete: %s | %d bytes", filePath, bytesRead));

        // PARSE

        dataJob.setStatus(DataJobStatus.READ_FILE_START);
        try (InputStream fileInputStream = new FileInputStream(filePath)) {
            dataParser.parse(dataJob, fileInputStream, parseCounter);
        } catch (IOException e) {
            dataJob.setStatus(DataJobStatus.READ_FILE_ERROR);
            dataJobRepository.save(dataJob);
            e.printStackTrace();
            return;
        }

        setJobStatus(dataJob.getSource(), dataJob, parseCounter);
    }

    public boolean validDiskSpace() throws IOException {

        File directory = new File(FETCH_DATA_DIR);

        Path path = Paths.get(FETCH_DATA_DIR);

        FileStore fs = fileStoreProvider.getFileStore(path);

        if (directory.exists() && directory.isDirectory() && directory.canWrite()) {
            long freeSpace = fs.getUsableSpace();

            return freeSpace >= DATA_DIR_MIN_FREE;
        }

        return false;
    }

    public int writeToFile(String filePath, InputStream inputStream) throws IOException {
        // TODO: fix with -1 as finish
        int bytesRead = 0;
        File file = new File(filePath);

        try (OutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return bytesRead;
    }

    public void setJobStatus(Source source, DataJob dataJob, ParseCounter parseCounter) {

        // 5% error rate, mark job as failed to figure out consistent error
        if (parseCounter.getNumErrors() > (parseCounter.getNumProcessed() / ERROR_RATE))

        {
            dataJob.setStatus(DataJobStatus.ERROR);
        }

        dataJob.setNumFetched(parseCounter.getNumFetched());
        dataJob.setNumProcessed(parseCounter.getNumProcessed());

        String logStats = String.format(
                "%s - %s: | Offset: %s | Fetched: %s | Skipped: %s | Built: %s | Processed: %s | Errors: %s | Duplicates: %s",
                source.getCategory(),
                source.getDescription(),
                dataJob.getParamOffset(),
                parseCounter.getNumFetched(),
                parseCounter.getNumSkipped(),
                parseCounter.getNumBuilt(),
                parseCounter.getNumProcessed(),
                parseCounter.getNumErrors(),
                parseCounter.getNumDuplicates());
        log.info(logStats);
    }

}
