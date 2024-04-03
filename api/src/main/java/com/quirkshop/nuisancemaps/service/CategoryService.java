package com.quirkshop.nuisancemaps.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.dto.CategoryGroupDTO;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {
    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

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
            // if dupe, catch, but ensure we have old parent for any new children
            if (text != null) {
                parent = categoryRepository.findByDataTypeAndTextAndLabel(dataType, text, label);
                if (parent == null) {
                    parent = new Category(dataType, text, label, null);
                    try {
                        parent = categoryRepository.save(parent);
                        num++;
                    } catch(DataIntegrityViolationException e) {
                        log.error(e.getMessage());
                    }
                }
            }

            // subcategories
            // if dupe, catch and move next
            for (Category subCategoryDTO : categoryDTO.getSubcategories()) {
                String childText = subCategoryDTO.getText();
                Integer childLabel = subCategoryDTO.getLabel();

                Category child = new Category(dataType, childText, childLabel, parent);

                try {
                    categoryRepository.save(child);
                    num++;
                } catch (DataIntegrityViolationException e) {
                    log.error(e.getMessage());
                }
            }

        }

        return num;
    }
}
