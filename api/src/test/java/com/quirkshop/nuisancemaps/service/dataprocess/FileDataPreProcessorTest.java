package com.quirkshop.nuisancemaps.service.dataprocess;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.config.DataParserType;
import com.quirkshop.nuisancemaps.model.Source;
import com.quirkshop.nuisancemaps.model.datajob.DataJob;
import com.quirkshop.nuisancemaps.repository.DataJobRepository;
import com.quirkshop.nuisancemaps.repository.LocaleRepository;
import com.quirkshop.nuisancemaps.repository.MappingRepository;
import com.quirkshop.nuisancemaps.repository.SourceRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileDataPreProcessorTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private FileDataProcessStrategy fileDataProcessStrategy;

    @MockBean
    private DataJobRepository dataJobRepository;

    @Autowired
    private FileDataPreProcessor fileDataPreProcessor;

    private static final String FETCH_DATA_DIR = System.getenv("FETCH_DATA_DIR");

    @Test
    @Transactional
    public void preProcessVanillaTest() throws IOException {
        String url = "https://www.data.gov/testopresto/test.json";

        Source source = new Source();
        source.setUrl(url);
        source.setDataParserType(DataParserType.CSV);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        dataJob.setUrl(url);

        // dataJob filePath unchanged, preProcess not triggered
        String fileName = dataJob.buildFilename();
        String filePath = String.join("/", FETCH_DATA_DIR, fileName);

        String preProcessFileName = fileDataPreProcessor.preProcess(dataJob, false);
        assertThat(preProcessFileName).isEqualTo(filePath);
    }

    @Test
    @Transactional
    public void preProcessZipFilePathTest() throws IOException {

        // tests for modified and correct filePath creation
        String zipFile = "123.csv";
        String url = "https://www.data.gov/testopresto/test/abc.zip?zipfile=" + zipFile;
        Source source = new Source();
        source.setUrl(url);
        source.setDataParserType(DataParserType.CSV);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        dataJob.setUrl(url);

        // dataJob filePath add hyphen with zipfile param value

        String fileName = dataJob.buildFilename();
        String filePath = String.join("/", FETCH_DATA_DIR, fileName) + "-" + zipFile;

        String preProcessFileName = fileDataPreProcessor.preProcess(dataJob, false);
        assertThat(preProcessFileName).isEqualTo(filePath);
    }

    @Test
    @Transactional
    public void preProcessZipTest() throws IOException {

        String url = "https://data.gov/testzip.zip?zipfile=crime-bos.csv";
        String extractFileName = "crime-bos.csv";

        Source source = new Source();
        source.setUrl(url);
        source.setDataParserType(DataParserType.CSV);
        DataJob dataJob = new DataJob(LocalDateTime.now(), source, "id");
        dataJob.setUrl(url);

        // copy testzip.zip fixture to /data/<dataJob.buildFilename()> as if downloaded
        String zipFileName = "testzip.zip";
        Resource sourceZip = resourceLoader.getResource("classpath:data/" + zipFileName);
        String zipFilePath = String.join("/", FETCH_DATA_DIR, dataJob.buildFilename());
        fileDataProcessStrategy.writeToFile(zipFilePath, sourceZip.getInputStream());

        // verify "downloaded" zip file exists
        File zipFile = new File(zipFilePath);
        assertThat(zipFile.exists()).isTrue();
        assertThat(zipFile.isFile()).isTrue();

        // test that extracted file initially does not exist
        String preProcessFilePath = fileDataPreProcessor.preProcess(dataJob, false);
        File preProcessFile = new File(preProcessFilePath);
        assertThat(preProcessFile.exists()).isFalse();

        // preProcess file - expect unzip operation and creation of preProcessFileName
        preProcessFilePath = fileDataPreProcessor.preProcess(dataJob, true);
        preProcessFile = new File(preProcessFilePath);

        assertThat(preProcessFile.exists()).isTrue();
        assertThat(preProcessFile.isFile()).isTrue();
        assertThat(preProcessFilePath.endsWith(extractFileName));
        assertThat(preProcessFile.length()).isGreaterThan(0);

        // cleanup
        zipFile.delete();
        preProcessFile.delete();

        assertThat(zipFile.exists()).isFalse();
        assertThat(preProcessFile.exists()).isFalse();
    }

}
