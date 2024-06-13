import { useState } from "react";
export default function FeatureView({ feature, initListMode }) {
  /*
   TODO: fix max width of card

 */
  if (!feature) return null;

  const [listMode, setListMode] = useState(initListMode);

  const properties = feature.properties;

  //clusters are set to string by maplibre
  //points are objects
  let category = feature.properties.category;
  if (typeof category == "string")
    category = JSON.parse(feature.properties.category);

  const dateOptions = { year: "2-digit", month: "short", day: "2-digit" };
  const reportedAtDate = new Date(
    feature.properties.reportedAt
  ).toLocaleDateString("en-US", dateOptions);

  console.log("Feature", properties);
  console.log("Category", category);
  const iconStyle = {
    fontFamily: "Font Awesome\\ 6 Free",
    fontWeight: 900,
  };

    /*
       TODO: restart w/ mobile get widths aligned
       icon | text | date - 
     */

  return (
    <li
      className=""
      style={{width: "400px"}}
      key={feature.properties.reportNum}
      onClick={() => setListMode(!listMode)}
    >
      <div>
        {/* head */}
        <div className="flex justify-between text-sm mb-2">
          <div className="flex gap-3">
            <span style={iconStyle}>{category.iconUnicode}</span>
            <span><strong>{feature.properties.reportCategory}</strong></span>
          </div>

          <div className="text-sm">{reportedAtDate}</div>
        </div>

        {/* details toggle expand*/}
        {!listMode && (
            <>
                <hr className="mb-2"/>
          <div className="flex justify-between text-sm mb-4">
              <dl>
              <dt className="text-neutral-content">Report ID</dt>
              <dd className="text-neutral mb-4">{feature.properties.reportNum}</dd>
              <dt className="text-neutral-content">Location</dt>
              <dd className="text-neutral mb-4">{feature.properties.location}</dd>
            </dl>
                      <dl>
                          <dt className="text-neutral-content">Report Type</dt>
                          <dd className="text-neutral text-right mb-4">{category.dataType}</dd>
                      </dl>

          </div>
            </>
        )}
      </div>
    </li>
  );
}
