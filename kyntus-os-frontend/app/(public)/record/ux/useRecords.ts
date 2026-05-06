import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";

// 🔥 THE MATRIX FIX: Dynamic URL Resolution
// 1. Kay-jbed l'IP mn l'Serveur (Docker Environment Variable)
// 2. Ila mal9ahch (f l'Localhost dyalek), kay-dir Fallback l 8083
const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8083";

const API_BASE = `${BASE_URL}/api/os/records/v1`; 
const BILLING_API = `${BASE_URL}/api/os/billing-engine`; 

export const useColumns = (category: string, year: number, month: number, version: string = "V2") => {
  return useQuery({
    queryKey: ["columns", category, year, month, version],
    queryFn: async () => {
      const res = await fetch(`${API_BASE}/columns?category=${category}&year=${year}&month=${month}&version=${version}`);
      if (!res.ok) throw new Error("Erreur fetching columns");
      return res.json() as Promise<string[]>;
    },
  });
};

export const useV1Records = (category: string, year: number, month: number, page: number, size: number, columns: string[], version: string = "V2") => {
  return useQuery({
    queryKey: ["records", category, year, month, version, page, size, columns],
    queryFn: async () => {
      const colParam = columns.length > 0 ? `&columns=${columns.join(",")}` : "";
      const res = await fetch(`${API_BASE}/data?category=${category}&year=${year}&month=${month}&version=${version}&page=${page}&size=${size}${colParam}`);
      if (!res.ok) throw new Error("Erreur fetching records data");
      return res.json();
    },
  });
};

// 🔥 NOUVEAU HOOK: Bach n-déclenchiw l'calcul f l'Backend
export const useRunBillingEngine = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ category, year, month }: { category: string; year: number; month: number }) => {
      const res = await fetch(`${BILLING_API}/execute?category=${category}&year=${year}&month=${month}`, {
        method: 'POST'
      });
      if (!res.ok) throw new Error("Erreur Engine");
      return res.json();
    },
    onSuccess: () => {
      // Mli y-sali l'calcul, refetch l'colonnes w data auto
      queryClient.invalidateQueries({ queryKey: ["columns"] });
      queryClient.invalidateQueries({ queryKey: ["records"] });
    }
  });
};