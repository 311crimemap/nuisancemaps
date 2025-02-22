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
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import com.quirkshop.nuisancemaps.WorkerApplication;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.model.datajob.DataJobStatus;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.service.dataparser.DataParser;
import com.quirkshop.nuisancemaps.util.ParseCounter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.logstash.logback.argument.StructuredArguments;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

@Service
public class FileDataProcessStrategy implements DataProcessStrategy {

    private static final String FETCH_DATA_DIR = System.getenv("FETCH_DATA_DIR");
    private static final long DATA_DIR_MIN_FREE = Long.parseLong(System.getenv("DATA_DIR_MIN_FREE"));
    private static final int BATCH_SIZE = Integer.parseInt(System.getenv("BATCH_SIZE"));

    @Autowired
    private FileStoreProvider fileStoreProvider;

    @Autowired
    private OkHttpClient client;

    @Autowired
    private DataJobRepository dataJobRepository;

    @Autowired
    private FileDataPreProcessor fileDataPreProcessor;

    private static final Logger log = LoggerFactory.getLogger(WorkerApplication.class);

    @Override
    public InputStream fetchData(DataJob dataJob) {
        InputStream inputStream = null;
        Response response = null;

        String url = dataJob.getUrl();
        Source source = dataJob.getSource();
        String cookie = source.getCookie();
        Builder requestBuilder = new Request.Builder().url(url);
        if (cookie != null) {
            requestBuilder.addHeader("Cookie", cookie);
        }
        Request request = requestBuilder.build();

        dataJob.setParamLimit(BATCH_SIZE); // NB: CSV download entire file
        dataJob.setStatus(DataJobStatus.FETCH_START);
        dataJobRepository.save(dataJob);

        try {

            // check if file exists before downloading
            String filePath = buildFilePath(dataJob);

            if (fileExists(filePath) && !dataJob.isForceDownload()) {
                File file = new File(filePath);
                long fileSizeInBytes = file.length();

                Map<String, Object> logDetails = Map.of(
                        "filePath", filePath,
                        "fileSizeInBytes", fileSizeInBytes);

                log.info("[FileDataProcessStrategy fetchData] File Detected",
                        StructuredArguments.entries(Map.of("data", logDetails)));

                if (fileSizeInBytes > 0) {
                    dataJob.setStatus(DataJobStatus.FETCH_COMPLETE);
                    dataJobRepository.save(dataJob);
                    return null;
                } else {
                    // orphaned 0 byte file, clean it up
                    file.delete();
                }
            }

            // otherwise fetch
            log.info("[FileDataProcessStrategy fetchData] request execute");
            response = client.newCall(request).execute();

            // 202 accept: typically indicates start of background job, requires
            // periodic poll check for generated requested file. Set status and
            // defer back to queue.

            if (response.code() == 202) {
                log.info("[FileDataProcessStrategy fetchData] code 202 detected");
                dataJob.setStatus(DataJobStatus.POLL_WAIT);
                dataJobRepository.save(dataJob);
                if (response != null) {
                    response.close();
                }
                return null;
            }

            if (!response.isSuccessful()) {
                throw new IOException("[FileDataProcessStrategy fetchData] Unexpected code " + response);
            }

            inputStream = response.body().byteStream();

        } catch (IOException e) {
            Map<String, Object> logDetails = Map.of("error", e.getMessage());
            log.error("[FileDataProcessStrategy fetchData] Error",
                    StructuredArguments.entries(Map.of("data", logDetails)));

            dataJob.setStatus(DataJobStatus.FETCH_ERROR);
            dataJobRepository.save(dataJob);
            e.printStackTrace();
            if (response != null) {
                response.close();
            }
            return null;
        }

        dataJob.setStatus(DataJobStatus.FETCH_COMPLETE);
        dataJobRepository.save(dataJob);

        return inputStream;
    }

    @Override
    public void process(DataJob dataJob, InputStream inputStream, DataParser dataParser) {

        ParseCounter parseCounter = new ParseCounter();
        long bytesRead = 0;
        String filePath = null;

        dataJob.setStatus(DataJobStatus.PROCESS_START);
        dataJobRepository.save(dataJob);

        /*
         * STREAM TO FILE
         */

        try {

            filePath = buildFilePath(dataJob);

            // check if file exists before downloading
            // otherwise fetch stream -> writeToFile
            if (fileExists(filePath) && !dataJob.isForceDownload()) {
                File file = new File(filePath);
                bytesRead = file.length();

                Map<String, Object> logDetails = Map.of(
                        "filePath", filePath,
                        "bytesRead", bytesRead);

                log.info("[FileDataProcessStrategy process] File Detected",
                        StructuredArguments.entries(Map.of("data", logDetails)));

            } else {

                // write fetch inputStream to file
                if (!validDiskSpace()) {
                    dataJob.setStatus(DataJobStatus.NO_SPACE_ERROR);
                    dataJobRepository.save(dataJob);
                    return;
                }

                bytesRead = writeToFile(filePath, inputStream);
                Map<String, Object> logDetails = Map.of(
                        "filePath", filePath,
                        "bytesRead", bytesRead);

                log.info("[FileDataProcessStrategy process] Write Complete",
                        StructuredArguments.entries(Map.of("data", logDetails)));
            }

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

        /*
         * PREPROCESS
         * intermediate conversion (zip, etc) to indicated DataParserType
         */

        filePath = fileDataPreProcessor.preProcess(dataJob, true);
        if (filePath == null) {
            dataJob.setStatus(DataJobStatus.PREPROCESS_ERROR);
            return;
        }

        /*
         * PARSE FILE
         */

        dataJob.setStatus(DataJobStatus.READ_FILE_START);
        dataJobRepository.save(dataJob);

        try (InputStream fileInputStream = new FileInputStream(filePath)) {
            File file = new File(filePath);
            dataJob.setStatus(DataJobStatus.PENDING);
            dataJobRepository.save(dataJob);

            dataParser.parse(dataJob, file, fileInputStream, parseCounter);

        } catch (IOException e) {
            dataJob.setStatus(DataJobStatus.READ_FILE_ERROR);
            dataJobRepository.save(dataJob);
            e.printStackTrace();
            return;
        }

        setJobStatus(dataJob.getSource(), dataJob, parseCounter);

    }

    @Override
    public void cleanup(DataJob dataJob) {

        fileDataPreProcessor.cleanup(dataJob);

        String filePath;

        try {
            filePath = buildFilePath(dataJob);
            File file = new File(filePath);
            if (file.exists()) {
                Map<String, Object> logDetails = Map.of("filePath", filePath);
                log.info("[FileDataProcessStrategy process] Deleting",
                        StructuredArguments.entries(Map.of("data", logDetails)));
                file.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
            dataJob.setStatus(DataJobStatus.CLEANUP_ERROR);
            dataJobRepository.save(dataJob);
        }
    }

    private String buildFilePath(DataJob dataJob) throws MalformedURLException {
        String filename = dataJob.buildFilename();
        String filePath = String.join("/", FETCH_DATA_DIR, filename);
        return filePath;
    }

    public boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
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

    private TimerTask createProgressTask(String filePath, AtomicLong totalBytesRead) {
        return new TimerTask() {
            @Override
            public void run() {
                Map<String, Object> logDetails = Map.of(
                        "filePath", filePath,
                        "bytesRead", totalBytesRead.get());

                log.info("[FileDataProcessStrategy process] Writing",
                        StructuredArguments.entries(Map.of("data", logDetails)));
            }
        };
    }

    public long writeToFile(String filePath, InputStream inputStream) throws IOException {
        AtomicLong totalBytes = new AtomicLong(0);
        File file = new File(filePath);
        Timer progressTimer = new Timer(true);

        try (OutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead = 0;

            TimerTask progressTask = createProgressTask(filePath, totalBytes);
            progressTimer.schedule(progressTask, 0, 5000); // Delay: 0ms, Period: 5000ms

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytes.addAndGet(bytesRead);
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            progressTimer.cancel();
        }

        return totalBytes.get();
    }

}
