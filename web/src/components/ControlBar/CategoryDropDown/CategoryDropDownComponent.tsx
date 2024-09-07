import { Category } from "../../../types/category";
import DropDown from "../DropDown/index";
import { activeCategoriesReducer } from "./CategoryFilterReducer";
import CheckBoxGroup from "./CheckBoxFilters/CheckBoxGroup.js";
import CheckBoxLabel from "./CheckBoxFilters/CheckBoxLabel";

/*
 * use dropdown button versus <details> / <summary> tags
 * because dropdown will close on click elsewhere;
 * detail / summary remains open unless explicitly clicked to minimize parent
 */
interface DropDownFilterProps {
  type: "crime" | "311";
  activeCategories: Category[];
  activeCategoriesDispatcher: activeCategoriesReducer;
}

export function DropDownFilter({
  type,
  activeCategories,
  activeCategoriesDispatcher,
}: DropDownFilterProps) {
  const parent = activeCategories.find((cat) => cat.id == type);

  return (
    <DropDown>
      <CheckBoxLabel
        key={`checkboxlabel-${parent ? parent.id : type}`}
        category={parent}
        categories={[]}
        activeCategoriesDispatcher={activeCategoriesDispatcher}
        inclusiveCheck={false}
      />

      <CheckBoxGroup
        key={`group-${parent ? parent.id : type}`}
        parent={parent}
        categories={activeCategories}
        activeCategoriesDispatcher={activeCategoriesDispatcher}
      />
    </DropDown>
  );
}
