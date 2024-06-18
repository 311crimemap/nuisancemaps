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
  const filtered_categories = categories.filter(
    (cat) => cat.parent && cat.parent.id == parent.id
  );

  const components = [];

  //HEADER label
  //if it's root level parent (all 311, all crime checkbox) skip it
  //this was added in dropdown, otherwise add it
  //otherwise this is subcategory
  if (parent.parent !== null) {
    components.push(
      <li className={`header`}>
        <CheckBoxLabel
          key={`checkboxlabel-${parent.id}`}
          category={parent}
          categories={filtered_categories}
          activeCategoriesDispatcher={activeCategoriesDispatcher}
        />
      </li>
    );
  }

  const checkBoxLabeledGroup = filtered_categories.map((category) => {
    const sub_categories = categories.filter(
      (c) => c.parent && c.parent.id == category.id
    );

    if (category.label !== null) {
      return (
        <li>
          <CheckBoxLabel
            key={`checkboxlabel-${category.id}`}
            category={category}
            categories={filtered_categories}
            activeCategoriesDispatcher={activeCategoriesDispatcher}
          />
        </li>
      );
    }

    return (
      <CheckBoxGroup
        key={`group-${category.id}`}
        parent={category}
        categories={sub_categories}
        activeCategoriesDispatcher={activeCategoriesDispatcher}
        depth={depth + 1}
      />
    );
  });

  return components.concat(
    <ul className={`${depth ? "ml-4" : ""}`}>{checkBoxLabeledGroup}</ul>
  );
}
