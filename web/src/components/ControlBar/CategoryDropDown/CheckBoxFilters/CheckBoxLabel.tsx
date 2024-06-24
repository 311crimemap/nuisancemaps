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

  const onFocusHandler = (e) => {
    // vanilla inputs, behave normally
    if (inclusiveCheck) return;

    // on checkbox click, prevent focus from opening dropdown
    // for use on "root" level "toggle all" labels
    console.log("[label] FOCUS");
    e.currentTarget.blur(); //close
    e.stopPropagation();
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
          <span className="label-text pl-2 w-full">{`${category.text}`}</span>
        </>
      )}
    </div>
  );
}
