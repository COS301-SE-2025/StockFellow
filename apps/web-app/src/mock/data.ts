export const stats = {
  totalPayouts: "R150,000",
  stokvels: 1250,
  growthRate: "+5.80%",
};

export const stokvelDetailsData = Array.from({ length: 20 }, (_, i) => ({
  name: `${i * 5}k`,
  value: Math.floor(Math.random() * 100),
}));

export const stokvelsCreatedData = [
  { month: "Jul", value: 20 },
  { month: "Aug", value: 40 },
  { month: "Sep", value: 60 },
  { month: "Oct", value: 80 },
  { month: "Nov", value: 50 },
  { month: "Dec", value: 70 },
  { month: "Jan", value: 65 },
];

export const requests = [
  { id: 1, name: "Obito Uchiha", type: "User" },
  { id: 2, name: "The Akatsuki", type: "Stokvel" },
  { id: 3, name: "Son Goku", type: "User" },
];

export const tierStats = [
  { name: "Tier 1", value: 30 },
  { name: "Tier 2", value: 15 },
  { name: "Tier 3", value: 35 },
  { name: "Tier 4", value: 20 },
];
