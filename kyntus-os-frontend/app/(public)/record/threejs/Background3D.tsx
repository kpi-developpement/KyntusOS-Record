"use client";

import { useRef } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { MeshDistortMaterial, Environment, Float } from '@react-three/drei';
import * as THREE from 'three';

function LiquidGlassBlob() {
  const blobRef = useRef<THREE.Mesh>(null);
  
  // Les lumières li gha y-tbe3ou l'souris w y-dwiw fo9 l'zaj
  const lightRoyalBlue = useRef<THREE.PointLight>(null);
  const lightEmerald = useRef<THREE.PointLight>(null);

  useFrame((state, delta) => {
    const time = state.clock.getElapsedTime();
    
    // L'INTERACTIVITÉ: Coordonnées dyal l'souris
    const targetX = (state.pointer.x * 8);
    const targetY = (state.pointer.y * 8);

    // 1. La lumière Royal Blue kat-tbe3 l'souris b wa7ed l'effet Lerp (Smooth)
    if (lightRoyalBlue.current) {
      lightRoyalBlue.current.position.x = THREE.MathUtils.lerp(lightRoyalBlue.current.position.x, targetX, 0.05);
      lightRoyalBlue.current.position.y = THREE.MathUtils.lerp(lightRoyalBlue.current.position.y, targetY, 0.05);
    }

    // 2. La lumière Emerald Green kat-dir l'3eks w kat-dour shwiya (Effet organique)
    if (lightEmerald.current) {
      lightEmerald.current.position.x = THREE.MathUtils.lerp(lightEmerald.current.position.x, -targetX * 0.5 + Math.sin(time) * 2, 0.05);
      lightEmerald.current.position.y = THREE.MathUtils.lerp(lightEmerald.current.position.y, -targetY * 0.5 + Math.cos(time) * 2, 0.05);
    }

    // 3. L'blob (l'zaja) kat-dour b shwiya
    if (blobRef.current) {
      blobRef.current.rotation.x += delta * 0.1;
      blobRef.current.rotation.y += delta * 0.15;
    }
  });

  return (
    <>
      {/* --- LES LUMIÈRES --- */}
      {/* Lumière ambiante blanche bach tbiyen l'fond n9i */}
      <ambientLight intensity={1.5} color="#ffffff" />
      <directionalLight position={[10, 10, 5]} intensity={2} color="#ffffff" />
      
      {/* Lumière interactive 1 (Royal Blue Dominant) */}
      <pointLight ref={lightRoyalBlue} color="#2563eb" intensity={25} distance={15} />
      
      {/* Lumière interactive 2 (Touche Emerald Green) */}
      <pointLight ref={lightEmerald} color="#10b981" intensity={15} distance={15} />

      {/* --- LE LIQUID GLASS (ZAJ SAYEL) --- */}
      {/* Float kay-khlih y-tla3 w yhbet b7al ila kay-flotti f l'ma */}
      <Float speed={2} rotationIntensity={0.5} floatIntensity={1}>
        <mesh ref={blobRef} scale={2.5} position={[0, 0, -2]}>
          <sphereGeometry args={[1, 128, 128]} />
          <MeshDistortMaterial
            color="#ffffff"
            roughness={0.1}        // Lisse bzaf bach y-reflecti do
            metalness={0.1}        // Shwiya dyal reflet métallique
            clearcoat={1}          // Couche de vernis (Glass effect)
            clearcoatRoughness={0.1}
            distort={0.4}          // Ch7al bghinah y-morphé w yt-3ewej
            speed={2}              // Vitesse dyal l'morphing
            transmission={0.9}     // MAGIE HNA: Transparence dyal l'zaj
            ior={1.5}              // Indice de Réfraction (b7al l'vrai zaj)
            thickness={1.5}        // Somk dyal l'zaj
          />
        </mesh>
      </Float>
    </>
  );
}

export default function Background3D() {
  return (
    // Fond Blanc Ultra-Clean (Slate-50)
    <div className="absolute inset-0 z-[-1] bg-[#f8fafc] overflow-hidden">
      {/* Gradient radial bach n-concentriw l'inara f l'wst */}
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,rgba(255,255,255,0.0)_0%,rgba(241,245,249,0.9)_100%)] z-10 pointer-events-none" />
      
      <Canvas camera={{ position: [0, 0, 6], fov: 50 }} className="z-0">
        {/* Environment map: Kanjibouhd l'HDRI mn drei bach l'zaj y-reflecti un environnement wa9i3i (b7al studio) */}
        <Environment preset="city" /> 
        <LiquidGlassBlob />
      </Canvas>
    </div>
  );
}