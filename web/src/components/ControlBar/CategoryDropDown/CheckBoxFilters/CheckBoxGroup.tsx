import { useState, useEffect, useReducer } from "react";
import CheckBoxLabel from "./CheckBoxLabel";

export default function CheckBoxGroup({
  parent,
  categories,
  activeCategoriesDispatcher,
  depth = 0,
}) {
  //parent, and then its filtered children
  //we decide if children are group or label

  //these are cats at same level
  const filtered_categories = categories.filter(
    (cat) => cat.parent && cat.parent.id == parent.id
  );

  /*
   * NB: this is a recusive component to enable nesting
   */
  const checkBoxLabeledGroup = filtered_categories.map((category) => {
    const sub_categories = categories.filter(
      (c) => c.parent && c.parent.id == category.id
    );

    return (
      <li>
        {/*
         * Parent: category with subcategories has details toggle
         *
         * NB: the grid-cols-1 is to extend the clickable width to the
         * whole parent width (and not just the text content of <label>)
         */}
        {sub_categories.length ? (
          <details open={false}>
            <summary className="grid grid-cols-1">
              <CheckBoxLabel
                key={`checkboxlabel-${parent.id}`}
                category={category}
                categories={filtered_categories}
                activeCategoriesDispatcher={activeCategoriesDispatcher}
              />
            </summary>
            <ul>
              <CheckBoxGroup
                key={`group-${category.id}`}
                parent={category}
                categories={sub_categories}
                activeCategoriesDispatcher={activeCategoriesDispatcher}
                depth={depth + 1}
              />
            </ul>
          </details>
        ) : (
          <>
            {/* Category (no subcategories, no toggle) */}
            <summary className="grid grid-cols-1">
              <CheckBoxLabel
                key={`checkboxlabel-${category.id}`}
                category={category}
                categories={filtered_categories}
                activeCategoriesDispatcher={activeCategoriesDispatcher}
              />
            </summary>
            <ul>
              <CheckBoxGroup
                key={`group-${category.id}`}
                parent={category}
                categories={sub_categories}
                activeCategoriesDispatcher={activeCategoriesDispatcher}
                depth={depth + 1}
              />
            </ul>
          </>
        )}
      </li>
    );
  });

  return checkBoxLabeledGroup;
}
