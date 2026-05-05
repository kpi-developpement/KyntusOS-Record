"use client";

import React, { useState, useMemo, useRef, useEffect } from "react";
import Providers from "./ux/Providers";
import { useColumns, useV1Records, useRunBillingEngine } from "./ux/useRecords";
import { Activity, Search, Check, Layers, ChevronDown, Calendar, Hash, Folder, Zap, Database } from "lucide-react";
import SmartTable from "./components/SmartTable";

// --- CUSTOM SMOOTH DROPDOWN COMPONENT (TRANSFORMER GLOW EFFECT) ---
const CustomSelect = ({ value, options, onChange, icon: Icon, placeholder, zIndexValue = 50 }: any) => {
  const [isOpen, setIsOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setIsOpen(false);
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const selectedLabel = options.find((o: any) => o.value === value)?.label || placeholder;

  return (
    <div className="relative w-full" style={{ zIndex: zIndexValue }} ref={ref}>
      <button 
        onClick={() => setIsOpen(!isOpen)} 
        // 🔥 TRANSFORMER HOVER: Glow Zre9 w Border sharp
        className="w-full flex items-center justify-between bg-white/70 backdrop-blur-md border border-blue-200/60 p-3.5 rounded-xl shadow-sm hover:bg-white hover:border-blue-500 hover:shadow-[0_0_15px_rgba(37,99,235,0.3)] transition-all duration-300 outline-none group"
      >
        <div className="flex items-center gap-3">
          <Icon size={18} className="text-blue-600 group-hover:scale-110 transition-transform duration-300" />
          <span className="text-slate-800 font-bold tracking-wide text-sm">{selectedLabel}</span>
        </div>
        <div style={{ transform: `rotate(${isOpen ? 180 : 0}deg)`, transition: '0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)' }}>
          <ChevronDown size={16} className="text-blue-500" />
        </div>
      </button>

      {isOpen && (
        <div 
          className="absolute top-full left-0 right-0 mt-2 bg-white/95 backdrop-blur-2xl border border-blue-300/50 rounded-xl shadow-[0_20px_50px_rgba(37,99,235,0.2)] overflow-hidden py-1 max-h-[350px] overflow-y-auto"
          style={{ zIndex: zIndexValue + 10 }}
        >
          {options.map((opt: any) => (
            <div 
              key={opt.value} 
              onClick={() => { onChange(opt.value); setIsOpen(false); }} 
              className={`p-3 mx-1 my-0.5 rounded-lg cursor-pointer text-sm transition-all duration-200 ${value === opt.value ? 'bg-blue-50 text-blue-700 font-black border-l-4 border-blue-600 shadow-[inset_4px_0_0_#2563eb]' : 'text-slate-600 hover:bg-slate-50 hover:pl-5 hover:text-blue-600 font-semibold border-l-4 border-transparent'}`}
            >
              {opt.label}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

function RecordDashboard() {
  const [category, setCategory] = useState("RACC");
  const [year, setYear] = useState(new Date().getFullYear());
  const [month, setMonth] = useState(new Date().getMonth() + 1);
  const [version, setVersion] = useState("V2"); 
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(50);
  
  const [selectedColumns, setSelectedColumns] = useState<string[]>([]);
  const [searchCol, setSearchCol] = useState("");

  const { data: availableColumns, isLoading: loadingCols } = useColumns(category, year, month, version);
  const { data: recordsData, isLoading: loadingRecords, isFetching } = useV1Records(category, year, month, page, pageSize, selectedColumns, version);
  
  const runEngine = useRunBillingEngine();

  useEffect(() => {
    if (version === "V2") {
      const requiredColumns = [
        "Forfait INST Kyntus", "Prix forfait INST Kyntus", 
        "Forfait INST Support Kyntus", "Prix Forfait INST support Kyntus", 
        "Forfait INTST- Kyntus", "Prix Forfait INTST Kyntus", 
        "Materiel prix2", "MES22 Kyntus", "Prix Forfait MES Kyntus", "Mt Kyntus", 
        "JARRETIERE_UNI", "PRIX_HT_JARRETIERE2", 
        "Forfait INST SST", "Prix Forfait SST", 
        "Materiel prix", "MES STT", "Prix Forfait MES SST", 
        "Forfait Logistique SST", "Prix Forfait Logistique SST", "Mt SST"
      ];
      setSelectedColumns(requiredColumns);
    } else {
      if (availableColumns && availableColumns.length > 0) {
        setSelectedColumns(availableColumns);
      }
    }
  }, [availableColumns, version]);

  const filteredCols = useMemo(() => {
    if (!availableColumns) return [];
    return availableColumns.filter((col: string) => col.toLowerCase().includes(searchCol.toLowerCase()));
  }, [availableColumns, searchCol]);

  const toggleColumn = (col: string) => {
    setSelectedColumns(prev => prev.includes(col) ? prev.filter(c => c !== col) : [...prev, col]);
  };

  const handleRunEngine = () => {
    runEngine.mutate({ category, year, month }, {
        onSuccess: () => {
            setVersion("V2"); 
        }
    });
  };

  const categoryOptions = [
    { value: "RACC", label: "RACC - Raccordement" },
    { value: "SAV", label: "SAV - Service Client" },
    { value: "FTTH", label: "FTTH - Fibre Optique" }
  ];
  const yearOptions = [2024, 2025, 2026].map(y => ({ value: y, label: `Année ${y}` }));
  const monthOptions = Array.from({length: 12}, (_, i) => ({ value: i+1, label: `Mois ${String(i+1).padStart(2, '0')}` }));
  
  const versionOptions = [
    { value: "V1", label: "V1 - Données Gringotts (Raw)" },
    { value: "V2", label: "V2 - Kyntus Billing Engine" }
  ];

  return (
    <div className="relative z-10 p-6 max-w-[1600px] mx-auto text-slate-800 font-sans min-h-screen flex flex-col">
      <div className="flex flex-col flex-1">
        
        <div className="flex justify-between items-center border-b border-blue-200/50 pb-6 mb-8 relative z-[5000]">
          <div className="flex items-center gap-6">
            <div className="p-4 bg-white/80 backdrop-blur-xl border border-blue-100 rounded-2xl shadow-sm hover:shadow-[0_0_20px_rgba(37,99,235,0.4)] transition-shadow duration-300">
              <Layers size={36} className="text-blue-600" />
            </div>
            <div>
              <h1 className="text-4xl md:text-5xl font-black tracking-tighter text-slate-900">
                KYNTUS <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-700 to-blue-400">NEXUS</span>
              </h1>
              <div className="text-xs font-bold tracking-[0.25em] text-emerald-600 mt-2 flex items-center gap-2 uppercase">
                {isFetching ? <Activity size={14} className="animate-spin text-blue-600" /> : <span className="w-2 h-2 bg-emerald-500 rounded-full animate-pulse inline-block" />}
                Deep State Matrix - {version}
              </div>
            </div>
          </div>
          
          {/* 🤖🔥 BOUTON TRANSFORMER PRIME 🔥🤖 */}
          <button 
            onClick={handleRunEngine}
            disabled={runEngine.isPending}
            className="
              relative overflow-hidden group
              flex items-center gap-2 
              bg-transparent border-2 border-blue-600 text-blue-600 
              font-black text-sm uppercase tracking-widest px-8 py-3.5 rounded-xl
              transition-all duration-300 ease-out
              hover:text-white hover:border-transparent hover:shadow-[0_0_25px_rgba(37,99,235,0.6)]
              active:scale-95 active:bg-gradient-to-r active:from-white active:to-blue-600 active:text-blue-900 active:border-white
              disabled:opacity-50 disabled:hover:scale-100 disabled:hover:text-blue-600 disabled:hover:bg-transparent disabled:hover:shadow-none
            "
          >
            {/* Effet dyal Sweep (T3mira) */}
            <span className="absolute inset-0 bg-blue-600 origin-left scale-x-0 group-hover:scale-x-100 transition-transform duration-[400ms] ease-[cubic-bezier(0.19,1,0.22,1)] z-0"></span>
            
            {/* L'contenu dyal l'bouton (Z-10 bach yb9a lfo9 mn t3mira) */}
            <span className="relative z-10 flex items-center gap-2">
              {runEngine.isPending ? <Activity size={18} className="animate-spin" /> : <Zap size={18} className="fill-current" />}
              {runEngine.isPending ? "Calculating Matrix..." : "Run Billing V2"}
            </span>
          </button>
        </div>

        <div className="grid grid-cols-1 xl:grid-cols-4 gap-8 flex-1 relative z-[1000]">
          
          <div className="xl:col-span-1 bg-white/60 backdrop-blur-2xl border border-white shadow-sm rounded-3xl p-7 h-fit relative z-[99999]">
            <div className="flex items-center gap-3 mb-6 relative z-10">
              <div className="w-1.5 h-6 bg-blue-600 rounded-full shadow-[0_0_10px_rgba(37,99,235,0.8)]"></div>
              <h2 className="text-slate-800 font-black tracking-widest text-sm">PERIOD MATRIX</h2>
            </div>
            
            <div className="flex flex-col gap-4 mb-8">
              <CustomSelect value={version} options={versionOptions} onChange={(v: string) => {setVersion(v); setPage(0);}} icon={Database} zIndexValue={9999} />
              <CustomSelect value={category} options={categoryOptions} onChange={(v: string) => {setCategory(v); setPage(0);}} icon={Folder} zIndexValue={9998} />
              <div className="flex gap-4">
                <CustomSelect value={year} options={yearOptions} onChange={(v: number) => {setYear(v); setPage(0);}} icon={Calendar} zIndexValue={9997} />
                <CustomSelect value={month} options={monthOptions} onChange={(v: number) => {setMonth(v); setPage(0);}} icon={Hash} zIndexValue={9996} />
              </div>
            </div>

            <div className="w-full h-px bg-gradient-to-r from-transparent via-blue-200 to-transparent my-8 relative z-10"></div>

            <div className="flex items-center gap-3 mb-5 relative z-10">
              <div className="w-1.5 h-6 bg-emerald-500 rounded-full shadow-[0_0_10px_rgba(16,185,129,0.8)]"></div>
              <h2 className="text-slate-800 font-black tracking-widest text-sm">PAYLOAD COLUMNS ({selectedColumns.length})</h2>
            </div>

            <div className="relative mb-5 z-10 group">
              <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <Search size={16} className="text-blue-500 group-hover:text-blue-600 transition-colors" />
              </div>
              <input 
                type="text" 
                placeholder="Scanner l'ADN..." 
                value={searchCol}
                onChange={(e) => setSearchCol(e.target.value)}
                className="w-full bg-white/80 hover:bg-white border border-blue-100 pl-11 pr-4 py-3.5 rounded-xl text-sm outline-none focus:ring-2 focus:ring-blue-500/50 hover:shadow-[0_0_15px_rgba(37,99,235,0.2)] text-slate-800 font-bold transition-all duration-300"
              />
            </div>

            {loadingCols ? (
              <div className="flex items-center gap-3 text-blue-600 text-sm font-bold p-4 bg-blue-50/80 rounded-xl relative z-10">
                <Activity size={16} className="animate-spin" /> Synchronisation...
              </div>
            ) : (
              <div className="flex flex-col gap-2 max-h-[350px] overflow-y-auto pr-2 custom-scrollbar relative z-10 pb-2">
                {filteredCols.map((col: string) => {
                  const isSelected = selectedColumns.includes(col);
                  return (
                    <label 
                      key={col} 
                      className={`flex items-center gap-3 p-3.5 rounded-xl cursor-pointer border transition-all duration-300 ${isSelected ? 'bg-blue-600 border-blue-600 shadow-[0_4px_15px_rgba(37,99,235,0.4)] hover:shadow-[0_4px_20px_rgba(37,99,235,0.6)]' : 'bg-white/80 border-white hover:bg-white hover:pl-5 hover:border-l-4 hover:border-l-blue-400'}`}
                    >
                      <div className={`w-5 h-5 flex items-center justify-center rounded-md border transition-colors ${isSelected ? 'bg-white border-white' : 'border-slate-300'}`}>
                        {isSelected && <Check size={14} className="text-blue-600 font-black" />}
                      </div>
                      <input type="checkbox" checked={isSelected} onChange={() => toggleColumn(col)} className="hidden" />
                      <span className={`text-sm font-bold truncate transition-colors ${isSelected ? 'text-white' : 'text-slate-700'}`}>{col}</span>
                    </label>
                  );
                })}
              </div>
            )}
          </div>

          <div className="xl:col-span-3 h-fit relative z-10">
             <SmartTable 
                data={recordsData} 
                loading={loadingRecords} 
                page={page} 
                setPage={setPage} 
                pageSize={pageSize} 
                setPageSize={setPageSize}
                selectedColumns={selectedColumns}
             />
          </div>

        </div>
      </div>
    </div>
  );
}

// 🔥 ROYAL BLUE INTERACTIVE BACKGROUND
import DeepStateBackground from "./components/DeepStateBackground";

export default function Page() {
  return (
    <main className="relative min-h-screen w-full overflow-hidden selection:bg-blue-600 selection:text-white">
      <DeepStateBackground />
      <Providers>
        <RecordDashboard />
      </Providers>
    </main>
  );
}