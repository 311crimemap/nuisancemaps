import { Category } from "../../../types/category";

export type ActiveCategoriesDispatcher = (
    action: {
        type: string;
        category?: any;
        categories?: Category[]
    }
) => void;

export default function categoryCheckBoxReducer(
  categories: Category[],
    action: {
        type: string;
        category?: any;
        categories?: Category[]
    }
): Category[] {

    console.log("ACTION", action, categories);
  switch (action.type) {
    //set default available here as delayed by async request
    case "init": {
      return action.categories || [];
    }
    //param id
    case "toggleCheckBoxById": {
      // basically a "top down" match for clicked category
      //
      // e.g. loop through all categories - if category's parent matches
      // the clicked category toggle that node

      const _categories = categories.map((category) => {
        //toggle immediate clicked label
        if (category.id == action.category.id) {
          return { ...category, checked: !category.checked };
        }

        //and traverse hierarchy of each category to find common parent
        let hasCommonParent = false;
        let parent: Category | null = category;
        while (parent) {
          if (parent.id == action.category.id) {
            hasCommonParent = true;
            break;
          }

          parent = parent.parent;
        }

        if (hasCommonParent)
          return { ...category, checked: !action.category.checked };

        return category;
      });

      // Consistent top level behavior: control bar checkbox needs to
      // reflect some checked (on) or all off (off)

      // Find master checkBox
      const masterCheckCategory = _categories.find((category) => {
        return (
          action.category.dataType == category.dataType &&
          category.label === null &&
          category.parent === null
        );
      });

      if (masterCheckCategory != null) {
        // filter for proper dataType (311 or crime and exclude the
        // masterCheckCategory)
        const cats = _categories.filter((category) => {
          return (
            category != masterCheckCategory &&
            category.dataType == action.category.dataType
          );
        });

        //if all children are off, check master off
        if (cats.every((cat) => !cat.checked)) {
          masterCheckCategory.checked = false;
        }

        //if any child is toggled on, toggle master on
        if (cats.some((cat) => cat.checked)) {
          masterCheckCategory.checked = true;
        }
      }

      return _categories;
    }

    default: {
      throw new Error("action doesn't exist " + action.type);
    }
  }
}
