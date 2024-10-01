import { DateRange } from "../../../types/daterange";

type action = {
  type: string;
  value: any;
};

export default function dateFilterReducer(
  filterDate: DateRange,
  action: action
): DateRange {
  switch (action.type) {
    case "calcDate": {
      const calcDate = new Date();
      const max = new Date();
      const endMaxDate = max.toLocaleDateString("en-CA");
      const value = action.value;

      // month
      if (value.includes("Month")) {
        const months = value.split(" ")[0];
        calcDate.setMonth(calcDate.getMonth() - months);
      }

      // week
      else if (value.includes("Week")) {
        const weeks = value.split(" ")[0];
        calcDate.setDate(calcDate.getDate() - 7 * Number(weeks));
      }

      // days
      else if (value.includes("Day")) {
        const days = value.split(" ")[0];
        calcDate.setDate(calcDate.getDate() - Number(days));
      }

      return {
        ...filterDate,
        label: value,
        date: {
          startDate: calcDate.toLocaleDateString("en-CA"),
          endDate: endMaxDate,
        },
      };
    }

    case "setDate": {
      return {
        ...filterDate,
        label: "custom",
        date: {
          ...filterDate.date,
          ...action.value,
        },
      };
    }
    case "isBusy": {
      return {
        ...filterDate,
        isBusy: action.value,
      };
    }
    default: {
      throw new Error("action doesn't exist: " + action.type);
    }
  }
  return filterDate;
}
