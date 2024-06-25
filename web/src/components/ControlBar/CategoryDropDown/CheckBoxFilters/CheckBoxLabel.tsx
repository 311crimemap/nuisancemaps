import { useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

export default function CheckBoxLabel({
  category,
  categories,
  activeCategoriesDispatcher,
  inclusiveCheck = true, //whether checkbox ste to trigger inclusive of clicking on text
}) {
  const checkHandler = (e) => {
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
    <div>
      {/* TODO: match to w-72 Dropdown index.tsx */}
      {inclusiveCheck ? (
        <label className="w-60 cursor-pointer">
          <input
            id={`${category.id}-checkbox`}
            type="checkbox"
            checked={category.checked}
            onChange={checkHandler}
            className="hidden"
          />
          <div class="fa-stack" style={circleStyle}>
            <span class="fa-regular fa-circle fa-stack-2x"></span>
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
