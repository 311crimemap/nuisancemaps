import { Link } from "react-router-dom";
import { CategoryMinMaxReportedAt } from "../../types/categoryminmaxreportedat";
import { slugify, dateFormat } from "../../Util";
import { StateCity } from "./statecitymap";

export default function CityData({ cityData }: any) {
  // build categories map
  const catMinMaxReport = (cityData: StateCity) => {
    const categories: { [key: string]: CategoryMinMaxReportedAt } = {};

    for (const cat of cityData.categoryMinMaxReportedAt) {
      categories[cat.category] = cat;
    }
    return categories;
  };

  const categories = catMinMaxReport(cityData);

  const minMaxReportedAt = (cat: CategoryMinMaxReportedAt) => {
    if (!cat) return null;

    const formattedCount = new Intl.NumberFormat("en-US", {
      minimumFractionDigits: 0,
    }).format(cat.count);

    return (
      <tr className="border-0">
        <td> {cat.category}</td>
        <td className="text-right">{dateFormat(cat.minReportedAt)}</td>
        <td className="text-right">{dateFormat(cat.maxReportedAt)}</td>
        <td className="text-right hidden md:table-cell">{formattedCount}</td>
        <td className="text-right hidden lg:table-cell">
          {dateFormat(cat.djMaxUpdatedAt)}
        </td>
      </tr>
    );
  };

  return (
    <li key={cityData.city} className="border-0 mt-6">
      <span className="text-lg">
        <Link to={`/${slugify(cityData.city)}`} className="link">
          {cityData.city}
        </Link>
      </span>

      <table className="table table-fixed table-sm border-collpase mt-3 ml-[-0.625rem]">
        <thead className="border-0">
          <tr>
            <th className="font-normal">Dataset</th>
            <th className="font-normal text-right">Start Date</th>
            <th className="font-normal text-right">End Date</th>
            <th className="font-normal text-right hidden md:table-cell">
              Records
            </th>
            <th className="font-normal text-right hidden lg:table-cell">
              Last Update
            </th>
          </tr>
        </thead>
        <tbody>
          {minMaxReportedAt(categories["crime"])}
          {minMaxReportedAt(categories["311"])}
        </tbody>
      </table>
    </li>
  );
}
