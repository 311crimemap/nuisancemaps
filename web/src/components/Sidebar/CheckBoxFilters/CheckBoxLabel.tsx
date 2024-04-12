import {useState, useEffect, useReducer} from "react";


export default function CheckBoxLabel({ category, categories, activeCategoriesDispatcher}) {

    const checkHandler = (e) => {

        activeCategoriesDispatcher({
            'type': 'toggleCheckBoxById',
            id: category.id,
            checked: category.checked
        });

        console.log("CLIKC", category, category.id, category.checked);
    }



    return (
        <div>
            <input id={`${category.id}-checkbox`}
                type="checkbox"
                checked={category.checked}
                onChange={checkHandler}
            />
            <label htmlFor={`${category.id}-checkbox`}>
                {`${category.text} - ${category.checked}`}
            </label>
        </div>
    )

}
