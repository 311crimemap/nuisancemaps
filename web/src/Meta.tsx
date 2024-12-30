import { Helmet, HelmetData } from "react-helmet-async";

interface MetaProps {
  pageName: string;
}

export default function Meta({ pageName }: MetaProps) {
  const helmetData = new HelmetData({});
  const title = pageName ? `311 Crime Map - ${pageName}` : "311 Crime Map";

  return (
        <Helmet helmetData={helmetData}>
            <title>{title}</title>
            <meta property="og:title" content={title} />
            <meta property="twitter:title" content={title} />
        </Helmet>
    );
}
