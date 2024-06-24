import { useState } from "react";

export default function CheckBoxLabel({
  category,
  categories,
  activeCategoriesDispatcher,
  inclusiveCheck = true, //whether checkbox ste to trigger inclusive of clicking on text
}) {
  const checkHandler = (e) => {
    console.log("[label] CHANGE");
    activeCategoriesDispatcher({
      type: "toggleCheckBoxById",
      category,
    });
  };

  // on checkbox click, prevent focus from opening dropdown
  // for use on "root" level "toggle all" labels
  const onFocusHandler = (e) => {
    console.log("[label] FOCUS");
    e.currentTarget.blur(); //close
    e.stopPropagation();
  };

  return (
    <div>
      {inclusiveCheck ? (
        <label>
          <input
            id={`${category.id}-checkbox`}
            type="checkbox"
            checked={category.checked}
            className="cursor-pointer"
            onChange={checkHandler}
          />
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
              onFocus={onFocusHandler}
              onChange={checkHandler}
            />
          </label>
          <span className="label-text pl-2">{`${category.text}`}</span>
        </>
      )}
    </div>
  );
}
