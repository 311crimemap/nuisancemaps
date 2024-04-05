package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.quirkshop.nuisancemaps.dto.CategoryGroupDTO;
import com.quirkshop.nuisancemaps.dto.TextLabelDTO;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.model.TextCategory;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;
import com.quirkshop.nuisancemaps.repository.TextCategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class TextCategoryServiceTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TextCategoryRepository textCategoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TextCategoryService textCategoryService;

    /*
     * test initMap() make sure build proper category labels
     * don't need parent categories, just labelled categories
     */
    @Test
    @Transactional
    public void postConstructorCategoryLabelTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Resource jsonResource = resourceLoader.getResource("classpath:data/classifier_categories.json");
        CategoryGroupDTO categoryGroupDTO = objectMapper.readValue(jsonResource.getFile(),
                CategoryGroupDTO.class);

        categoryService.createCategoriesDTO(categoryGroupDTO);

        textCategoryService.initMaps();

        HashMap<Integer, Integer> data311CategoryLabelToIdMap = textCategoryService.getData311CategoryLabelToIdMap();
        HashMap<Integer, Integer> dataCrimeCategoryLabelToIdMap = textCategoryService.getDataCrimeCategoryLabelToIdMap();

        // count number of categories with labels (no parents, just
        // subcategories)

        int num311 = 0;
        int numCrime = 0;
        for (Category c : categoryGroupDTO.getData311s()) {
            num311 += c.getSubcategories().size();
        }
        for (Category c : categoryGroupDTO.getDataCrimes()) {
            numCrime += c.getSubcategories().size();
        }

        assertThat(data311CategoryLabelToIdMap.size()).isEqualTo(num311);
        assertThat(dataCrimeCategoryLabelToIdMap.size()).isEqualTo(numCrime);
    }

    @Test
    @Transactional
    public void postConstructorTextCategoryIdTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Resource jsonResource = resourceLoader.getResource("classpath:data/classifier_categories.json");
        CategoryGroupDTO categoryGroupDTO = objectMapper.readValue(jsonResource.getFile(),
                CategoryGroupDTO.class);

        categoryService.createCategoriesDTO(categoryGroupDTO);

        List<Category> cat311s = categoryRepository.findAllByDataType("311");
        List<Category> catCrimes = categoryRepository.findAllByDataType("crime");
        TextCategory t1 = new TextCategory("311", "test1", cat311s.get(0));
        TextCategory t2 = new TextCategory("crime", "test2", catCrimes.get(0));
        textCategoryRepository.save(t1);
        textCategoryRepository.save(t2);

        textCategoryService.initMaps();

        HashMap<String, Integer> data311TextCatMap = textCategoryService.getData311TextToCategoryIdMap();
        HashMap<String, Integer> dataCrimeTextCatMap = textCategoryService.getDataCrimeTextToCategoryIdMap();

        assertThat(data311TextCatMap.size()).isEqualTo(1);
        assertThat(dataCrimeTextCatMap.size()).isEqualTo(1);

        assertThat(data311TextCatMap.get(t1.getText())).isEqualTo(t1.getCategory().getId());
        assertThat(dataCrimeTextCatMap.get(t2.getText())).isEqualTo(t2.getCategory().getId());
    }

    @Test
    @Transactional
    public void createTextCategoriesTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Resource jsonResource = resourceLoader.getResource("classpath:data/classifier_categories.json");
        CategoryGroupDTO categoryGroupDTO = objectMapper.readValue(jsonResource.getFile(),
                CategoryGroupDTO.class);

        categoryService.createCategoriesDTO(categoryGroupDTO);

        textCategoryService.initMaps();

        List<TextLabelDTO> textLabelDTOs = new ArrayList<TextLabelDTO>() {
            {
                add(new TextLabelDTO("311", "test0", 0));
                add(new TextLabelDTO("311", "test1", 1));
                add(new TextLabelDTO("crime", "test1 bleep", 0));
                add(new TextLabelDTO("crime", "test2 bloop", 1));
                add(new TextLabelDTO("crime", "test3", 2));
            }
        };

        Iterable<TextCategory> res = textCategoryService.createTextCategories(textLabelDTOs);
        List<TextCategory> results = ImmutableList.copyOf(res);

        assertThat(results.size()).isEqualTo(textLabelDTOs.size());

        assertThat(results.size()).isEqualTo(textCategoryRepository.count());

        for (TextLabelDTO textLabelDTO : textLabelDTOs) {

            TextCategory textCat = textCategoryRepository
                    .findByDataTypeAndText(textLabelDTO.getDataType(), textLabelDTO.getText());

            assertThat(textCat).isNotNull();
        }

        //non-existent labels - should not be saved to db (no mapping)
        textLabelDTOs = new ArrayList<TextLabelDTO>() {
                {
                    add(new TextLabelDTO("311", "test100", 100));
                    add(new TextLabelDTO("311", "test101", 101));

                }
            };

        res = textCategoryService.createTextCategories(textLabelDTOs);
        results = ImmutableList.copyOf(res);
        assertThat(results.size()).isEqualTo(0);
    }
}
