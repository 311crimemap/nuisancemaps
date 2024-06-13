import { useState } from "react";
export default function FeatureView({ feature, featuresLen, isLast, initListMode }) {
  /*
   TODO: fix max width of card

 */
  if (!feature) return null;

  const [listMode, setListMode] = useState(initListMode);
  const properties = feature.properties;

  //clusters are set to string by maplibre
  //points are objects
  let category = properties.category;
  if (typeof category == "string")
    category = JSON.parse(properties.category);

  const dateOptions = { year: "2-digit", month: "short", day: "2-digit" };
  const reportedAtDate = new Date(
    properties.reportedAt
  ).toLocaleDateString(navigator.language || "en-US", dateOptions);

  const iconStyle = {
    fontFamily: "Font Awesome\\ 6 Free",
    fontWeight: 900,
    fontSize: "1.5rem",
    alignContent: "center",
    padding: "0 .25rem",
  };

  return (
    <li
      className="w-full sm:w-96"
      key={properties.reportNum}
      onClick={() => setListMode(!listMode)}
    >
      <div>
        {/* head */}
        <div className="min-h-8 flex justify-between items-center text-sm mb-2 cursor-pointer">
          <div className="flex gap-3">
            <span style={iconStyle}>{category.iconUnicode}</span>
            <span>
              <strong>{properties.reportCategory}</strong>
            </span>
          </div>

          <div className="w-24 text-sm text-right">{reportedAtDate}</div>
        </div>

        {listMode && !isLast && <hr className="mb-2" />}

        {/* details toggle expand*/}
        {!listMode && (
          <>
            <div className="flex justify-between text-sm">
              <dl>
                <dt className="text-neutral-content">Report ID</dt>
                <dd className="text-neutral mb-4">
                  {properties.reportNum}
                </dd>
                <dt className="text-neutral-content">Location</dt>
                <dd className="text-neutral mb-4">
                  {properties.location}
                </dd>
              </dl>
              <dl>
                <dt className="text-neutral-content">Report Type</dt>
                <dd className="text-neutral text-right mb-4">
                  {category.dataType}
                </dd>
              </dl>
            </div>
            {!isLast && <hr className="mb-2" />}
          </>
        )}
      </div>
    </li>
  );
}
