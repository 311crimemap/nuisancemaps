package com.quirkshop.nuisancemaps.repository;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.model.Category;

@SpringBootTest(classes = NuisancemapsApplication.class)

public class CategoryRepositoryTest {

    @Autowired
    CategoryRepository categoryRepository;

    @Test
    @Transactional
    public void CategoryFindByDataTypeAndTextAndLabel() throws Exception {
        Category cat = new Category("311", "IamText", 1, null);
        cat = categoryRepository.save(cat);

        Category cat2 = categoryRepository.findByDataTypeAndTextAndLabel("311", "IamText", 1);
        assertThat(cat.getId()).isEqualTo(cat2.getId());

        Category cat3 = categoryRepository.findByDataTypeAndTextAndLabel("311", "IamNotText", 1);
        assertThat(cat3).isNull();
    }

}
