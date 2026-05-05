"use client";

import React, { memo } from 'react';
import { Loader2, Database, Layers } from 'lucide-react';

interface SmartTableProps {
  data: any; 
  loading: boolean;
  page: number;
  setPage: (p: number) => void;
  pageSize: number;
  setPageSize: (s: number) => void;
  selectedColumns: string[];
}

const SmartTable = memo(function SmartTable({ data, loading, page, setPage, pageSize, setPageSize, selectedColumns }: SmartTableProps) {
  
  const records = data?.content || [];
  const totalPages = data?.totalPages || 0;
  const totalElements = data?.totalElements || 0;

  // Kat-jbed l'valeur kima bghat tkoun (0 awla SAV-0.0 awla N/A)
  const getDynamicValue = (row: any, colName: string) => {
    if (!row || !row.dynamicData) return <span className="text-slate-300">-</span>;
    
    const targetKey = colName.trim().toLowerCase();
    
    for (const [key, value] of Object.entries(row.dynamicData)) {
      if (key.trim().toLowerCase() === targetKey) {
        if (value === "NON_TROUVE" || value === "N/A" || value === "") {
            return <span className="text-red-500 font-extrabold text-xs px-2 py-1 bg-red-50 rounded-md shadow-sm border border-red-100">N/A</span>;
        }
        if (value === 0 || value === 0.0 || value === "0" || value === "0.0") {
            return <span className="text-slate-400 font-bold">0</span>;
        }
        // Les valeurs String b7al "SAV-0.0" w les Prix ghaybano hna
        return <span className="font-semibold text-slate-700">{value as string | number}</span>;
      }
    }
    return <span className="text-slate-300">-</span>;
  };

  const getColumnStyle = (colName: string, isHeader: boolean = false) => {
    // 🔥 ZEDNA LES INPUTS DYAL SAV (DEPLACEMENT, TOTAL NET initial) HNA:
    const kyntusCols = [
      "Forfait INST Kyntus", "Prix forfait INST Kyntus", "Forfait INST Support Kyntus", "Prix Forfait INST support Kyntus", 
      "Forfait INTST- Kyntus", "Prix Forfait INTST Kyntus", "Materiel prix2", "MES22 Kyntus", "Prix Forfait MES Kyntus", "Mt Kyntus",
      "INSTALLATION", "MATERIEL", "MES", "SUPPORT", "LOGISTIQUE", "E1_MES", 
      "DEPLACEMENT", "TOTAL NET initial" // <- SAV Specific V1
    ];
    
    const sstCols = [
      "Forfait INST SST", "Prix Forfait SST", "Materiel prix", "MES STT", "Prix Forfait MES SST", 
      "Forfait Logistique SST", "Prix Forfait Logistique SST", "Mt SST"
    ];

    const targetKey = colName.trim().toLowerCase();

    // 🟧 Couleur Kyntus (Lymouni dyal Excel)
    if (kyntusCols.some(k => k.toLowerCase() === targetKey)) {
      return isHeader 
        ? { backgroundColor: "#fbd8bd", color: "#984807", borderBottom: "2px solid #f8cbad" }
        : { backgroundColor: "rgba(253, 233, 217, 0.6)", color: "#984807" }; 
    }
    
    // 🟥 Couleur SST (L'7mer dyal Excel)
    if (sstCols.some(k => k.toLowerCase() === targetKey)) {
      return isHeader 
        ? { backgroundColor: "#d99694", color: "#632523", borderBottom: "2px solid #c0504d" }
        : { backgroundColor: "rgba(229, 184, 183, 0.4)", color: "#632523" };
    }

    // 🟦 Couleur Standard (Zre9 Mftou7)
    return isHeader 
      ? { backgroundColor: "#f8fafc", color: "#475569", borderBottom: "2px solid #e2e8f0" }
      : { backgroundColor: "transparent", color: "#334155" };
  };

  return (
    <div className="flex flex-col h-full w-full relative z-10 group/table">
      
      {/* 👑 PREMIUM HEADER */}
      <div className="flex justify-between items-end mb-6 px-1">
        <div className="flex items-center gap-4">
          <div className="p-3 bg-gradient-to-br from-blue-500 to-blue-700 rounded-2xl shadow-[0_8px_16px_rgba(37,99,235,0.2)] group-hover/table:shadow-[0_8px_25px_rgba(37,99,235,0.4)] transition-shadow duration-300">
            <Database size={24} className="text-white" />
          </div>
          <div>
            <h3 className="text-2xl font-black text-slate-900 tracking-tight">DataGrid Explorer</h3>
            <p className="text-xs font-bold text-blue-600 uppercase tracking-widest mt-0.5">{totalElements} Cibles Identifiées</p>
          </div>
        </div>
        
        <div className="flex items-center gap-3">
          <span className="text-xs font-bold text-slate-400 uppercase tracking-widest">Lignes</span>
          <select 
            value={pageSize} 
            onChange={(e) => { setPageSize(Number(e.target.value)); setPage(0); }}
            className="bg-white border border-slate-200 text-slate-700 text-sm font-black rounded-xl px-4 py-2 outline-none hover:shadow-[0_0_15px_rgba(37,99,235,0.15)] focus:ring-2 focus:ring-blue-500/40 cursor-pointer hover:border-blue-400 transition-all duration-300"
          >
            {[50, 100, 200, 500].map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
      </div>

      {/* 👑 PREMIUM TABLE WRAPPER */}
      <div className="flex-1 bg-white ring-1 ring-slate-200/60 shadow-[0_20px_60px_-15px_rgba(0,0,0,0.05)] hover:shadow-[0_20px_60px_-15px_rgba(37,99,235,0.1)] rounded-[2rem] overflow-hidden flex flex-col relative transition-shadow duration-500">
        
        {loading && (
          <div className="absolute inset-0 z-50 bg-white/70 backdrop-blur-md flex flex-col items-center justify-center transition-all duration-300">
            <Loader2 size={36} className="text-blue-600 animate-spin mb-4" />
            <p className="text-blue-700 font-black tracking-widest text-xs uppercase animate-pulse">Synchronisation de la Matrice...</p>
          </div>
        )}

        <div className="overflow-x-auto flex-1 custom-scrollbar">
          <table className="w-full text-left border-collapse whitespace-nowrap">
            
            <thead className="sticky top-0 z-30 shadow-sm backdrop-blur-xl bg-white/90">
              <tr>
                <th className="py-4 px-6 text-[11px] font-extrabold text-slate-400 uppercase tracking-widest border-b-2 border-slate-100 w-12">#</th>
                <th className="py-4 px-6 text-[11px] font-extrabold text-slate-600 uppercase tracking-widest border-b-2 border-slate-100">EPS REF</th>
                <th className="py-4 px-6 text-[11px] font-extrabold text-slate-600 uppercase tracking-widest border-b-2 border-slate-100 text-center">Version</th>
                
                {selectedColumns.map(col => (
                  <th 
                    key={col} 
                    style={getColumnStyle(col, true)}
                    className="py-4 px-6 text-[11px] font-extrabold uppercase tracking-widest"
                  >
                    {col}
                  </th>
                ))}
              </tr>
            </thead>
            
            {!loading && records.length === 0 ? (
              <tbody>
                <tr>
                  <td colSpan={100} className="py-32 text-center bg-slate-50/50">
                    <Layers size={48} className="mx-auto text-slate-300 mb-4" />
                    <span className="text-slate-500 font-bold text-lg">Matrice Vide</span>
                    <p className="text-slate-400 text-sm mt-1">Aucune donnée trouvée pour cette période.</p>
                  </td>
                </tr>
              </tbody>
            ) : (
              <tbody className="divide-y divide-slate-100/60 bg-white">
                {records.map((row: any, index: number) => (
                  // 🔥 TRANSFORMER ROW HOVER: Glow + Inset Shadow
                  <tr key={row.id} className="hover:bg-blue-50/80 transition-all duration-300 group hover:shadow-[inset_4px_0_0_#2563eb]">
                    
                    <td className="py-3.5 px-6 text-xs font-bold text-slate-400 group-hover:text-blue-500 transition-colors">{page * pageSize + index + 1}</td>
                    
                    <td className="py-3.5 px-6 text-sm font-black text-slate-800">
                      <span className="group-hover:text-transparent group-hover:bg-clip-text group-hover:bg-gradient-to-r group-hover:from-blue-700 group-hover:to-blue-400 transition-all duration-300">{row.epsReference || "-"}</span>
                    </td>
                    
                    <td className="py-3.5 px-6 text-center">
                      <span className={`inline-flex items-center justify-center px-3 py-1 rounded-full text-[10px] font-black tracking-widest uppercase shadow-sm ${row.version === 'V2' ? 'bg-gradient-to-r from-blue-600 to-blue-500 text-white' : 'bg-slate-100 text-slate-500 border border-slate-200'}`}>
                        {row.version || "V1"}
                      </span>
                    </td>
                    
                    {selectedColumns.map(col => (
                      <td 
                        key={col} 
                        style={getColumnStyle(col, false)}
                        className="py-3.5 px-6 text-sm group-hover:brightness-95 transition-all duration-200"
                      >
                        {getDynamicValue(row, col)}
                      </td>
                    ))}

                  </tr>
                ))}
              </tbody>
            )}
          </table>
        </div>

        {/* 👑 PREMIUM PAGINATION */}
        <div className="bg-white border-t border-slate-100 p-4 px-6 flex items-center justify-between z-20">
          <div className="text-sm font-bold text-slate-400">
            Page <span className="text-slate-800 font-black">{records.length > 0 ? page + 1 : 0}</span> sur {totalPages}
          </div>
          <div className="flex gap-2">
            <button 
              onClick={() => setPage(page - 1)} 
              disabled={page === 0 || loading} 
              className="px-5 py-2 rounded-xl bg-slate-50 border border-slate-200 text-slate-600 font-bold hover:bg-white hover:text-blue-600 hover:border-blue-400 hover:shadow-[0_0_15px_rgba(37,99,235,0.2)] disabled:opacity-40 disabled:hover:shadow-none transition-all duration-300"
            >
              Précédent
            </button>
            <button 
              onClick={() => setPage(page + 1)} 
              disabled={page >= totalPages - 1 || loading} 
              className="px-5 py-2 rounded-xl bg-slate-50 border border-slate-200 text-slate-600 font-bold hover:bg-white hover:text-blue-600 hover:border-blue-400 hover:shadow-[0_0_15px_rgba(37,99,235,0.2)] disabled:opacity-40 disabled:hover:shadow-none transition-all duration-300"
            >
              Suivant
            </button>
          </div>
        </div>

      </div>
    </div>
  );
});

export default SmartTable;