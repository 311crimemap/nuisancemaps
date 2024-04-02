package com.quirkshop.nuisancemaps.service;

import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.dto.CategoryGroupDTO;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public CategoryService() {
    }

    public int createCategoriesDTO(CategoryGroupDTO categoryGroupDTO) {
        int numCrime = createCategories(categoryGroupDTO.getDataCrimes(), "crime");
        int num311 = createCategories(categoryGroupDTO.getData311s(), "311");
        return numCrime + num311;
    }

    public int createCategories(List<Category> categories, String dataType) {
        int num = 0;

        for (Category categoryDTO : categories) {
            String text = categoryDTO.getText();
            Integer label = categoryDTO.getLabel();

            Category parent = null;

            // parent
            if (text != null) {
                parent = new Category(dataType, text, label, null);
                parent = categoryRepository.save(parent);
                num++;
            }

            // subcategories
            ArrayList<Category> cats = new ArrayList<Category>();
            for (Category subCategoryDTO : categoryDTO.getSubcategories()) {
                String childText = subCategoryDTO.getText();
                Integer childLabel = subCategoryDTO.getLabel();

                Category child = new Category(dataType, childText, childLabel, parent);
                cats.add(child);
            }

            categoryRepository.saveAll(cats);
            num += cats.size();

        }

        return num;
    }
}
