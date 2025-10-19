import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from "recharts";
import { stokvelDetailsData } from "../../mock/data";

const StokvelDetailsChart = () => {
  return (
    <ResponsiveContainer width="100%" height={250}>
      <LineChart data={stokvelDetailsData}>
        <XAxis dataKey="name" />
        <YAxis />
        <Tooltip />
        <Line type="monotone" dataKey="value" stroke="#2563eb" strokeWidth={2} dot />
      </LineChart>
    </ResponsiveContainer>
  );
};

export default StokvelDetailsChart;
