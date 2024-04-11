import {useState, useEffect} from "react";

export default function CheckBoxLabel({id, text, parentChecked, setParentChecked, filterIdFn}) {

    const [checked, setChecked] = useState(parentChecked)

    const checkHandler = (e) => {
        setChecked(!checked);

        if (setParentChecked) {
            setParentChecked(!checked);
        }

        //call the data filter
        //filterIdFn(id)
    };

    //works as parent override
    useEffect( () => {
        setChecked(parentChecked);
    }, [parentChecked])

    return (
            <div>
                <input id={`${id}-checkbox`}
                       type="checkbox"
                       defaultChecked={checked}
                       checked={checked}
                       onChange={checkHandler}
                />
                <label htmlFor={`${id}-checkbox`}>
                    {`${text}`}
                </label>
            </div>
    )

}
