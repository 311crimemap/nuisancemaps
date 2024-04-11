import { useState, useEffect } from "react";
import CheckBoxLabel from "./CheckBoxLabel";

export default function CheckBoxGroup({ dataType, parent, dataTypeChecked, categories }) {

    const [parentChecked, setParentChecked] = useState(dataTypeChecked);

    //reset on dataTypeChecked - parent override
    useEffect( () => {
        setParentChecked(dataTypeChecked);
    }, [dataTypeChecked])


    function renderCategories(categories, parentChecked) {
        return (
            <ul>
                {
                    categories.map( (category) => {

                        return (
                            <li>
                                <CheckBoxLabel key={`checkboxlabel-${category.id}`}
                                               id={category.id}
                                               text={category.text}
                                               parentChecked={parentChecked}/>
                            </li>)
                    })
                }
            </ul>
        )
    }


    if (parent) {
        return (
            <ul>
                <li>
                    <CheckBoxLabel key={`checkboxlabel-${parent.id}`}
                                   id={parent.id}
                                   text={parent.text}
                                   setParentChecked={setParentChecked}
                                   parentChecked={parentChecked}/>
                </li>
                {
                    renderCategories(categories, parentChecked)
                }
            </ul>
        )
    }

    return renderCategories(categories, parentChecked);

}
