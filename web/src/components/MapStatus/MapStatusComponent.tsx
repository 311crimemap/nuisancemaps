import { DATASTATUS } from "../../types/datastatus";

interface MapStatusProps {
  dataStatus: DATASTATUS;
  errorRefreshFn: () => void;
}

export default function MapStatus({
  dataStatus,
  errorRefreshFn,
}: MapStatusProps) {
  switch (dataStatus) {
    case DATASTATUS.LOADING:
      return (
        <div id="spinner" className="flex flex-col items-center z-10">
          <span className="loading loading-spinner text-error loading-lg mb-4"></span>
          <span>Loading</span>
        </div>
      );
    case DATASTATUS.ERROR:
      return (
        <div className="toast toast-center z-10">
          <div className="alert alert-error">
            <span>There was a Loading Error.</span>
            <button className="btn btn-sm btn-outline" onClick={errorRefreshFn}>
              <span>
                <i className="fa fa-rotate-right" aria-hidden="true"></i>
                &nbsp; Retry
              </span>
            </button>
          </div>
        </div>
      );
    default:
      return null;
  }
}
