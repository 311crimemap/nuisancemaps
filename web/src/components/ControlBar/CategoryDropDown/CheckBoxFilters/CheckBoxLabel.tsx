export default function CheckBoxLabel({
  category,
  categories,
  activeCategoriesDispatcher,
}) {
  const checkHandler = (e) => {
    activeCategoriesDispatcher({
      type: "toggleCheckBoxById",
      category,
    });

    console.log("CLICK", category, category.id, category.checked);
  };

  return (
    <div>
      <label>
        <input
          id={`${category.id}-checkbox`}
          type="checkbox"
          checked={category.checked}
          onChange={checkHandler}
        />
        <span className="label-text">{`${category.text} - ${category.checked}`}</span>
      </label>
    </div>
  );
}
