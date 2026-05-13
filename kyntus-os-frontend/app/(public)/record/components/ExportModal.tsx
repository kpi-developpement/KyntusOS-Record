"use client";

import React, { useState, useRef, useEffect } from "react";
import { Download, X, CalendarRange, Loader2 } from "lucide-react"; // 🔥 Zedna Loader2 hna

interface ExportModalProps {
  category: string;
  version: string;
}

export default function ExportModal({ category, version }: ExportModalProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false); // 🔥 State jdid dyal l'chargement
  const modalRef = useRef<HTMLDivElement>(null);
  
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth() + 1;

  const [startYear, setStartYear] = useState<number>(currentYear);
  const [startMonth, setStartMonth] = useState<number>(currentMonth);
  const [endYear, setEndYear] = useState<number>(currentYear);
  const [endMonth, setEndMonth] = useState<number>(currentMonth);

  // Close modal when clicking outside (Khelina disabled ila kan kay-chargi bach may-seddouch b lghalat)
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (modalRef.current && !modalRef.current.contains(event.target as Node) && !isLoading) {
        setIsOpen(false);
      }
    };
    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [isOpen, isLoading]);

  const handleExport = async () => {
    try {
      setIsLoading(true); // 🔥 Kanch3lou l'loader

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"; // T2kked mn l'port dyal backend dyalek
      // 🔥 Zedna autoCalculate=true hna f l'kher
      const url = `${baseUrl}/api/os/records/v1/export?category=${category}&version=${version}&startYear=${startYear}&startMonth=${startMonth}&endYear=${endYear}&endMonth=${endMonth}&autoCalculate=true`;

      // N-siftou la requête b fetch
      const response = await fetch(url);

      if (!response.ok) {
        throw new Error("Mouchkil f l'API dyal l'export");
      }

      // N-jibou l'fichier 3la chkel Blob
      const blob = await response.blob();
      
      // N-creyiw lien f l'memoire w n-téléchariw bih
      const downloadUrl = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = downloadUrl;
      
      // N-smiw l'fichier (awla njibouh mn les headers ila bghina)
      const filename = `Kyntus_Export_${category}_${version}_${startYear}-${startMonth}_au_${endYear}-${endMonth}.csv`;
      link.setAttribute("download", filename);
      
      document.body.appendChild(link);
      link.click();
      
      // N-nkiw l'memoire
      document.body.removeChild(link);
      window.URL.revokeObjectURL(downloadUrl);

      // N-seddou l'modal melli ysali
      setIsOpen(false);
    } catch (error) {
      console.error("Erreur lors de l'export :", error);
      alert("Erreur: Wa7d l'mouchkil w9e3 f l'exportation. T2kked mn l'backend.");
    } finally {
      setIsLoading(false); // 🔥 Kanteffiw l'loader
    }
  };

  const years = [2024, 2025, 2026, 2027, 2028];
  const months = Array.from({ length: 12 }, (_, i) => i + 1);

  return (
    <div className="relative inline-block text-left z-[6000]">
      {/* 🔥 THE BUTTON */}
      <button 
        onClick={() => !isLoading && setIsOpen(!isOpen)}
        disabled={isLoading}
        className={`
          relative overflow-hidden group
          flex items-center gap-2 
          bg-white/80 backdrop-blur-md border border-blue-200 text-slate-800 
          font-black text-sm uppercase tracking-widest px-6 py-3.5 rounded-xl
          transition-all duration-300 ease-out shadow-sm
          ${isLoading ? 'opacity-70 cursor-not-allowed' : 'hover:border-blue-500 hover:text-blue-700 hover:shadow-[0_0_15px_rgba(37,99,235,0.3)] active:scale-95'}
        `}
      >
        {isLoading ? (
          <Loader2 size={18} className="text-blue-600 animate-spin" />
        ) : (
          <Download size={18} className="text-blue-600 group-hover:scale-110 transition-transform duration-300" />
        )}
        <span>{isLoading ? 'Export...' : 'Export CSV'}</span>
      </button>

      {/* 🌑 THE POPOVER MODAL */}
      {isOpen && (
        <div 
          ref={modalRef}
          className="absolute right-0 mt-3 w-80 bg-white/95 backdrop-blur-2xl border border-blue-300/50 rounded-2xl shadow-[0_20px_50px_rgba(37,99,235,0.2)] overflow-hidden animate-in fade-in zoom-in-95 duration-200"
        >
          {/* Header */}
          <div className="flex justify-between items-center bg-blue-50/80 px-5 py-4 border-b border-blue-100">
            <div className="flex items-center gap-2 text-slate-800 font-black tracking-widest text-xs uppercase">
              <CalendarRange size={16} className="text-blue-600" />
              <h3>Intervalle ({version})</h3>
            </div>
            <button 
              onClick={() => !isLoading && setIsOpen(false)} 
              disabled={isLoading}
              className="text-slate-400 hover:text-blue-600 transition-colors disabled:opacity-50"
            >
              <X size={18} />
            </button>
          </div>

          {/* Body */}
          <div className="p-5 space-y-5">
            {/* Start Date */}
            <div className={`${isLoading ? 'opacity-50 pointer-events-none' : ''}`}>
              <label className="block text-xs font-bold text-slate-500 mb-1.5 uppercase tracking-wide">De (Mois / Année)</label>
              <div className="flex gap-2">
                <select 
                  value={startMonth} 
                  onChange={(e) => setStartMonth(Number(e.target.value))}
                  className="flex-1 bg-white border border-blue-200 text-slate-700 font-semibold text-sm rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500/50 outline-none transition-all"
                >
                  {months.map(m => <option key={m} value={m}>Mois {String(m).padStart(2, '0')}</option>)}
                </select>
                <select 
                  value={startYear} 
                  onChange={(e) => setStartYear(Number(e.target.value))}
                  className="flex-1 bg-white border border-blue-200 text-slate-700 font-semibold text-sm rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500/50 outline-none transition-all"
                >
                  {years.map(y => <option key={y} value={y}>{y}</option>)}
                </select>
              </div>
            </div>

            {/* End Date */}
            <div className={`${isLoading ? 'opacity-50 pointer-events-none' : ''}`}>
              <label className="block text-xs font-bold text-slate-500 mb-1.5 uppercase tracking-wide">À (Mois / Année)</label>
              <div className="flex gap-2">
                <select 
                  value={endMonth} 
                  onChange={(e) => setEndMonth(Number(e.target.value))}
                  className="flex-1 bg-white border border-blue-200 text-slate-700 font-semibold text-sm rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500/50 outline-none transition-all"
                >
                  {months.map(m => <option key={m} value={m}>Mois {String(m).padStart(2, '0')}</option>)}
                </select>
                <select 
                  value={endYear} 
                  onChange={(e) => setEndYear(Number(e.target.value))}
                  className="flex-1 bg-white border border-blue-200 text-slate-700 font-semibold text-sm rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500/50 outline-none transition-all"
                >
                  {years.map(y => <option key={y} value={y}>{y}</option>)}
                </select>
              </div>
            </div>

            {/* Action Button */}
            <button
              onClick={handleExport}
              disabled={isLoading}
              className={`
                w-full flex justify-center items-center gap-2 mt-2 py-3 
                text-white font-black uppercase tracking-widest text-xs rounded-xl 
                shadow-[0_4px_15px_rgba(37,99,235,0.4)] transition-all
                ${isLoading ? 'bg-blue-400 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700 active:scale-95'}
              `}
            >
              {isLoading ? (
                <>
                  <Loader2 size={16} className="animate-spin" />
                  Calcul & Export...
                </>
              ) : (
                <>
                  <Download size={16} />
                  Générer CSV
                </>
              )}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}