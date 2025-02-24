package com.quirkshop.nuisancemaps.service;

import java.util.List;

import com.quirkshop.nuisancemaps.dto.CategoryGroupDTO;
import com.quirkshop.nuisancemaps.model.Category;
import com.quirkshop.nuisancemaps.repository.CategoryRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

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

    /**
     * Creates a list of categories and subcategories, ensuring hierarchy and
     * handling duplicates.
     *
     * @param categories the list of categories to be created
     * @param dataType   the type of data (e.g., "crime" or "311") associated with
     *                   the categories
     * @return the total number of categories successfully created and saved to the
     *         repository
     */
    public int createCategories(List<Category> categories, String dataType) {
        int num = 0;

        for (Category categoryDTO : categories) {
            String text = categoryDTO.getText();
            Integer label = categoryDTO.getLabel();
            String iconName = categoryDTO.getIconName();
            String iconUnicode = categoryDTO.getIconUnicode();

            Category parent = null;

            // parent category creation
            // catch any dupes; ensure consistent parent for the new children
            if (text != null) {
                parent = categoryRepository.findByDataTypeAndTextAndLabel(dataType, text, label);
                if (parent == null) {
                    parent = new Category(dataType, text, label, null, iconName, iconUnicode);
                    try {
                        parent = categoryRepository.save(parent);
                        num++;
                    } catch (DataIntegrityViolationException e) {
                        log.error(e.getMessage());
                    }
                }
            }

            // subcategories child creation
            // if dupe, catch and move next
            for (Category subCategoryDTO : categoryDTO.getSubcategories()) {
                String childText = subCategoryDTO.getText();
                Integer childLabel = subCategoryDTO.getLabel();

                Category child = new Category(dataType, childText, childLabel, parent, iconName, iconUnicode);

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
