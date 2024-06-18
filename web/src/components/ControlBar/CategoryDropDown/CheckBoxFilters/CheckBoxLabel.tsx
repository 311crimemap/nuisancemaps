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
          <label>
            <input
              id={`${category.id}-checkbox`}
              type="checkbox"
              checked={category.checked}
              className="cursor-pointer"
              onChange={checkHandler}
            />
          </label>
          <span className="label-text pl-2">{`${category.text}`}</span>
        </>
      )}
    </div>
  );
}
