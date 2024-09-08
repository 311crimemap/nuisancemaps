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

      if (value.includes("month")) {
        const months = value.split(" ")[0];
        calcDate.setMonth(calcDate.getMonth() - months);
        return {
          ...filterDate,
          date: {
            endDate: endMaxDate,
            startDate: calcDate.toLocaleDateString("en-CA"),
          },
        };
      }

      if (!isNaN(Number(value))) {
        calcDate.setDate(calcDate.getDate() - Number(value));
        return {
          ...filterDate,
          date: {
            endDate: endMaxDate,
            startDate: calcDate.toLocaleDateString("en-CA"),
          },
        };
      }
      break;
    }
    case "setDate": {
      return {
        ...filterDate,
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
