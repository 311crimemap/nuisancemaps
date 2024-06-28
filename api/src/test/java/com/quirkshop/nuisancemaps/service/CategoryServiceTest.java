package com.quirkshop.nuisancemaps.service;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.dto.CategoryGroupDTO;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class CategoryServiceTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Test
    @Transactional
    public void createCategories() throws IOException {
        Resource jsonResource = resourceLoader.getResource("classpath:data/classifier_categories.json");
        CategoryGroupDTO categoryGroupDTO = objectMapper.readValue(jsonResource.getFile(),
                CategoryGroupDTO.class);
        int numCategories311 = 0;
        int numCategoriesCrime = 0;
        int hasParent = 0;

        for (Category cat : categoryGroupDTO.getData311s()) {
            hasParent = cat.getText() != null ? 1 : 0;
            numCategories311 += cat.getSubcategories().size() + hasParent;
        }

        for (Category cat : categoryGroupDTO.getDataCrimes()) {
            hasParent = cat.getText() != null ? 1 : 0;
            numCategoriesCrime += cat.getSubcategories().size() + hasParent;
        }

        int num = categoryService.createCategoriesDTO(categoryGroupDTO);

        long numRecords = categoryRepository.count();
        List<Category> res311s = categoryRepository.findAllByDataType("311");
        List<Category> resCrimes = categoryRepository.findAllByDataType("crime");

        assertThat(num).isEqualTo(numRecords);
        assertThat(numRecords).isEqualTo(numCategories311 + numCategoriesCrime);
        assertThat(res311s.size()).isEqualTo(numCategories311);
        assertThat(resCrimes.size()).isEqualTo(numCategoriesCrime);

        // parent - child
        resCrimes.forEach(category -> {
            assertThat(category.getParent()).isNull();
        });

        for (Category category: res311s) {

            //edge case - internal use to indicate categories to skip
            if (category.getText() != null && category.getText().equals("SKIP")) {
                assertThat(category.getParent()).isNull();
                continue;
            }

            //text w/ no label - must be parent
            if (category.getLabel() == null && category.getText() != null) {
                // is a parent
                assertThat(category.getParent()).isNull();
            }

            // text w/ label - can be own parent (like crime), or child of a parent
        }

    }
}
