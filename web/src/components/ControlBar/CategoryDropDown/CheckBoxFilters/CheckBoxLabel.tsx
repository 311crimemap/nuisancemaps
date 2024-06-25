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
    marginTop: ".25rem",
    marginLeft: ".6rem",
    verticalAlign: "bottom",
  };
  const inputStyle = {
    //verticalAlign: "middle"
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
            style={inputStyle}
            onChange={checkHandler}
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
