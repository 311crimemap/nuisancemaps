import { Category } from "../../../../types/category";
import { ActiveCategoriesDispatcher } from "../CategoryFilterReducer";

interface CheckBoxLabelProps {
  category: Category;
  activeCategoriesDispatcher: ActiveCategoriesDispatcher;
  categories: Category[];
  inclusiveCheck?: boolean;
}

export default function CheckBoxLabel({
  category,
  activeCategoriesDispatcher,
  inclusiveCheck = true, //whether checkbox ste to trigger inclusive of clicking on text
}: CheckBoxLabelProps) {
  const checkHandler = () => {

    activeCategoriesDispatcher({
      type: "toggleCheckBoxById",
      category,
    });
  };

  const iconStyle = {
    fontFamily: "Font Awesome\\ 6 Free",
    fontWeight: 900,
  };

  const circleStyle = {
    display: "inline-flex",
    justifyContent: "center",
    fontSize: ".75rem",
    marginLeft: "-.5rem",
    verticalAlign: "baseline",
    opacity: category.checked ? 1 : 0.25,
  };

  return (
    <div className="w-100 block">
      {/* TODO: match to w-72 Dropdown index.tsx */}
      {inclusiveCheck ? (
        <label className="w-100 block cursor-pointer">
          <input
            id={`${category.id}-checkbox`}
            type="checkbox"
            checked={category.checked}
            onChange={checkHandler}
            className="hidden"
          />
          <div className="fa-stack" style={circleStyle}>
            <span className="fa-regular fa-circle fa-stack-2x"></span>
            <span style={iconStyle}>{category.iconUnicode}</span>
          </div>
          <span className="label-text pl-2">{`${category.text}`}</span>
        </label>
      ) : (
        <>
          {/* for root level categories with "checkbox all" toggle behavior */}
          <label>
            <input
              id={`${category.id}-checkbox`}
              type="checkbox"
              checked={category.checked}
              className="cursor-pointer"
              onChange={checkHandler}
            />
          </label>
          <span className="label-text pl-2 w-full">{`${category.text}`}</span>
        </>
      )}
    </div>
  );
}
