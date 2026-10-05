
import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from 'recharts';
//import { Activity, ShieldCheck, RefreshCw } from 'lucide-react';
import { Activity, ShieldCheck, RefreshCw, Flame, Zap, Moon, HeartPulse, Trash2, SlidersHorizontal, X } from 'lucide-react';

export default function App() {
  const [telemetryData, setTelemetryData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [simulating, setSimulating] = useState(false)
  const [resetting, setResetting] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState(1);


// Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [activeType, setActiveType] = useState('cardio');
  const [durationValue, setDurationValue] = useState(45);
  const [durationUnit, setDurationUnit] = useState('minutes'); // 'minutes' or 'hours'
  const [includeHR, setIncludeHR] = useState(true);


  // Fetch telemetry data from your Spring Boot REST API Controller
const fetchTelemetry = async () => {
  setLoading(true);
  try {
    const response = await fetch(`http://localhost:8080/api/v1/telemetry/deduplicate/${selectedUserId}`);
    const data = await response.json();
    
    // Explicitly sort chronologically before setting state
    const sortedData = [...data].sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
    setTelemetryData(sortedData);
  } catch (error) {
    console.error('Error fetching telemetry data:', error);
  } finally {
    setLoading(false);
  }
};


// OLD VERSION OF FETCH TELEMETRY (without time sorting)
  // const fetchTelemetry = async () => {
  //   setLoading(true);
  //   try {
  //     // Points to your Spring Boot REST endpoint
  //     const response = await fetch(`http://localhost:8080/api/v1/telemetry/deduplicate/${selectedUserId}`);
  //     const data = await response.json();
  //     setTelemetryData(data);
  //   } catch (error) {
  //     console.error('Error fetching telemetry data:', error);
  //   } finally {
  //     setLoading(false);
  //   }
  // };

// Reset database endpoint call
  const handleResetData = async () => {
    if (!window.confirm("Are you sure you want to delete all telemetry data for this user from the database?")) {
      return;
    }
    setResetting(true);
    try {
      await fetch(`http://localhost:8080/api/v1/telemetry/reset/${selectedUserId}`, {
        method: 'DELETE',
      });
      await fetchTelemetry();
    } catch (error) {
      console.error('Error resetting telemetry data:', error);
    } finally {
      setResetting(false);
    }
  };



// for new buttons:
// const triggerSimulation = async (type) => {
//     setSimulating(true);
//     try {
//       await fetch(`http://localhost:8080/api/v1/telemetry/simulate/${selectedUserId}?type=${type}`, {
//         method: 'POST',
//       });
//       await fetchTelemetry();
//     } catch (error) {
//       console.error('Simulation trigger failed:', error);
//     } finally {
//       setSimulating(false);
//     }
//   };





 // Open modal pre-filled with sensible defaults per activity type
  const openSimulationModal = (type) => {
    setActiveType(type);
    if (type === 'sleep') {
      setDurationValue(8);
      setDurationUnit('hours');
    } else if (type === 'nap') {
      setDurationValue(45);
      setDurationUnit('minutes');
    } else if (type === 'hiit') {
      setDurationValue(45);
      setDurationUnit('minutes');
    } else { // cardio
      setDurationValue(30);
      setDurationUnit('minutes');
    }
    setIncludeHR(true);
    setIsModalOpen(true);
  };

  // Trigger custom backend simulation based on modal configuration
  const handleSimulateSubmit = async (e) => {
    e.preventDefault();
    setSimulating(true);
    setIsModalOpen(false);

    // Convert hours to minutes if hours was selected
    const durationInMinutes = durationUnit === 'hours' 
      ? Math.round(parseFloat(durationValue) * 60) 
      : parseInt(durationValue, 10);

    try {
      const url = `http://localhost:8080/api/v1/telemetry/simulate/${selectedUserId}/custom?type=${activeType}&durationMinutes=${durationInMinutes}&generateHeartRate=${includeHR}`;
      await fetch(url, { method: 'POST' });
      await fetchTelemetry();
    } catch (error) {
      console.error('Simulation trigger failed:', error);
    } finally {
      setSimulating(false);
    }
  };

  useEffect(() => {
    fetchTelemetry();
  }, [selectedUserId]);

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 p-8 font-sans relative">
      {/* Header */}
      <header className="max-w-6xl mx-auto flex justify-between items-center mb-8 pb-4 border-b border-slate-800">
        <div className="flex items-center gap-3">
          <Activity className="w-8 h-8 text-emerald-400" />
          <h1 className="text-2xl font-bold tracking-tight">FitSync Telemetry Engine</h1>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={handleResetData}
            disabled={resetting || loading}
            className="flex items-center gap-2 bg-rose-950/80 hover:bg-rose-900 text-rose-300 border border-rose-800/50 px-4 py-2 rounded-lg font-medium transition cursor-pointer text-sm"
          >
            <Trash2 className={`w-4 h-4 ${resetting ? 'animate-pulse' : ''}`} />
            {resetting ? 'Clearing...' : 'Reset Data'}
          </button>
          <button
            onClick={fetchTelemetry}
            disabled={loading}
            className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-2 rounded-lg font-medium transition cursor-pointer text-sm"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            {loading ? 'Processing...' : 'Sync & Deduplicate'}
          </button>
        </div>
      </header>

      {/* Main Content Dashboard */}
      <main className="max-w-6xl mx-auto space-y-6">
        {/* Status Banner */}
        <div className="bg-slate-800 p-6 rounded-xl border border-slate-700 flex justify-between items-center">
          <div>
            <h2 className="text-lg font-semibold text-slate-200">Gold Standard Pipeline Status</h2>
            <p className="text-sm text-slate-400 mt-1">
              Active Priority: <span className="text-emerald-400 font-mono">AppleWatch (1)</span> &gt; <span className="text-blue-400 font-mono">Strava_API (2)</span>
            </p>
          </div>
          <div className="flex items-center gap-2 bg-emerald-950/80 text-emerald-400 px-3 py-1.5 rounded-full border border-emerald-800/50 text-sm">
            <ShieldCheck className="w-4 h-4" />
            <span>Algorithm Online</span>
          </div>
        </div>

        {/* Interactive Event Generator Panel */}
        <div className="bg-slate-800 p-6 rounded-xl border border-slate-700">
          <h3 className="text-md font-semibold text-slate-300 mb-1">Live Workout & Activity Telemetry Generator</h3>
          <p className="text-xs text-slate-400 mb-4">Click an activity to configure parameters and generate realistic time-series telemetry.</p>
          
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            <button
              onClick={() => openSimulationModal('cardio')}
              disabled={simulating}
              className="flex items-center justify-center gap-2 bg-slate-700 hover:bg-orange-600 text-slate-100 p-3 rounded-lg font-medium transition cursor-pointer text-sm"
            >
              <Flame className="w-4 h-4 text-orange-400" />
              <span>Strava Cardio</span>
            </button>
            <button
              onClick={() => openSimulationModal('hiit')}
              disabled={simulating}
              className="flex items-center justify-center gap-2 bg-slate-700 hover:bg-red-600 text-slate-100 p-3 rounded-lg font-medium transition cursor-pointer text-sm"
            >
              <Zap className="w-4 h-4 text-red-400" />
              <span>HIIT Strength</span>
            </button>
            <button
              onClick={() => openSimulationModal('sleep')}
              disabled={simulating}
              className="flex items-center justify-center gap-2 bg-slate-700 hover:bg-indigo-600 text-slate-100 p-3 rounded-lg font-medium transition cursor-pointer text-sm"
            >
              <Moon className="w-4 h-4 text-indigo-400" />
              <span>Log Sleep</span>
            </button>
            <button
              onClick={() => openSimulationModal('nap')}
              disabled={simulating}
              className="flex items-center justify-center gap-2 bg-slate-700 hover:bg-teal-600 text-slate-100 p-3 rounded-lg font-medium transition cursor-pointer text-sm"
            >
              <HeartPulse className="w-4 h-4 text-teal-400" />
              <span>Log Nap</span>
            </button>
          </div>
        </div>

        {/* Chart View */}
        <div className="bg-slate-800 p-6 rounded-xl border border-slate-700">
          <h3 className="text-md font-semibold text-slate-300 mb-4">Heart Rate Time Series (Deduplicated Stream)</h3>
          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={telemetryData}>
                <XAxis dataKey="timestamp" stroke="#64748b" tickFormatter={(str) => str.split('T')[1]?.slice(0, 8) || str} />
                <YAxis stroke="#64748b" domain={['dataMin - 5', 'dataMax + 5']} />
                <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155' }} />
                <Legend />
                <Line type="monotone" dataKey="heartRate" name="Heart Rate (BPM)" stroke="#10b981" strokeWidth={2} dot={{ fill: '#10b981' }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Data Stream Table */}
        <div className="bg-slate-800 rounded-xl border border-slate-700 overflow-hidden">
          <div className="p-4 border-b border-slate-700">
            <h3 className="text-md font-semibold text-slate-300">Resolved Telemetry Logs</h3>
          </div>
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-800/50 text-slate-400 uppercase text-xs border-b border-slate-700">
              <tr>
                <th className="px-6 py-3">Timestamp</th>
                <th className="px-6 py-3">Device Source</th>
                <th className="px-6 py-3">Heart Rate</th>
                <th className="px-6 py-3">Activity Claimed</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/50">
              {telemetryData.length === 0 ? (
                <tr>
                  <td colSpan="4" className="px-6 py-8 text-center text-slate-500">
                    No telemetry records found. Use the generator above to simulate events.
                  </td>
                </tr>
              ) : (
                telemetryData.map((row, idx) => (
                  <tr key={idx} className="hover:bg-slate-750">
                    <td className="px-6 py-4 font-mono text-xs">{row.timestamp}</td>
                    <td className="px-6 py-4">
                      <span className="px-2 py-1 rounded text-xs font-semibold bg-slate-700 text-slate-200">
                        {row.deviceSource}
                      </span>
                    </td>
                    <td className="px-6 py-4 font-medium text-emerald-400">{row.heartRate} BPM</td>
                    <td className="px-6 py-4 text-slate-400 text-xs font-mono">{row.activityTypeClaimed}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </main>

      {/* Modal GUI for Telemetry Generator Parameters */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-xs flex items-center justify-center z-50 p-4">
          <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 w-full max-w-md shadow-2xl relative">
            <button 
              onClick={() => setIsModalOpen(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-200 cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-2 mb-4">
              <SlidersHorizontal className="w-5 h-5 text-emerald-400" />
              <h3 className="text-lg font-semibold text-slate-100 capitalize">
                Simulate {activeType} Activity
              </h3>
            </div>

            <form onSubmit={handleSimulateSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1">
                  Activity Duration
                </label>
                <div className="flex gap-2">
                  <input
                    type="number"
                    min="1"
                    max="1440"
                    value={durationValue}
                    onChange={(e) => setDurationValue(e.target.value)}
                    className="bg-slate-900 border border-slate-700 text-slate-100 rounded-lg px-3 py-2 text-sm w-full focus:outline-none focus:border-emerald-500"
                    required
                  />
                  <select
                    value={durationUnit}
                    onChange={(e) => setDurationUnit(e.target.value)}
                    className="bg-slate-900 border border-slate-700 text-slate-100 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-emerald-500 cursor-pointer"
                  >
                    <option value="minutes">Minutes</option>
                    <option value="hours">Hours</option>
                  </select>
                </div>
              </div>

              <div className="flex items-center gap-3 pt-2">
                <input
                  type="checkbox"
                  id="includeHR"
                  checked={includeHR}
                  onChange={(e) => setIncludeHR(e.target.checked)}
                  className="w-4 h-4 rounded border-slate-700 text-emerald-600 focus:ring-emerald-500 focus:ring-offset-slate-800 cursor-pointer"
                />
                <label htmlFor="includeHR" className="text-sm text-slate-300 cursor-pointer">
                  Generate synthetic heart rate stream
                </label>
              </div>

              <div className="flex justify-end gap-2 pt-4 border-t border-slate-700">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 text-sm font-medium text-slate-400 hover:text-slate-200 cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={simulating}
                  className="bg-emerald-600 hover:bg-emerald-500 text-white text-sm font-medium px-4 py-2 rounded-lg transition cursor-pointer"
                >
                  {simulating ? 'Generating...' : 'Generate Telemetry'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}


// DEFAULT CODE:

// import { useState } from 'react'
// import reactLogo from './assets/react.svg'
// import viteLogo from './assets/vite.svg'
// import heroImg from './assets/hero.png'
// import './App.css'

// function App() {
//   const [count, setCount] = useState(0)

//   return (
//     <>
//       <section id="center">
//         <div className="hero">
//           <img src={heroImg} className="base" width="170" height="179" alt="" />
//           <img src={reactLogo} className="framework" alt="React logo" />
//           <img src={viteLogo} className="vite" alt="Vite logo" />
//         </div>
//         <div>
//           <h1>Get started</h1>
//           <p>
//             Edit <code>src/App.jsx</code> and save to test <code>HMR</code>
//           </p>
//         </div>
//         <button
//           type="button"
//           className="counter"
//           onClick={() => setCount((count) => count + 1)}
//         >
//           Count is {count}
//         </button>
//       </section>

//       <div className="ticks"></div>

//       <section id="next-steps">
//         <div id="docs">
//           <svg className="icon" role="presentation" aria-hidden="true">
//             <use href="/icons.svg#documentation-icon"></use>
//           </svg>
//           <h2>Documentation</h2>
//           <p>Your questions, answered</p>
//           <ul>
//             <li>
//               <a href="https://vite.dev/" target="_blank">
//                 <img className="logo" src={viteLogo} alt="" />
//                 Explore Vite
//               </a>
//             </li>
//             <li>
//               <a href="https://react.dev/" target="_blank">
//                 <img className="button-icon" src={reactLogo} alt="" />
//                 Learn more
//               </a>
//             </li>
//           </ul>
//         </div>
//         <div id="social">
//           <svg className="icon" role="presentation" aria-hidden="true">
//             <use href="/icons.svg#social-icon"></use>
//           </svg>
//           <h2>Connect with us</h2>
//           <p>Join the Vite community</p>
//           <ul>
//             <li>
//               <a href="https://github.com/vitejs/vite" target="_blank">
//                 <svg
//                   className="button-icon"
//                   role="presentation"
//                   aria-hidden="true"
//                 >
//                   <use href="/icons.svg#github-icon"></use>
//                 </svg>
//                 GitHub
//               </a>
//             </li>
//             <li>
//               <a href="https://chat.vite.dev/" target="_blank">
//                 <svg
//                   className="button-icon"
//                   role="presentation"
//                   aria-hidden="true"
//                 >
//                   <use href="/icons.svg#discord-icon"></use>
//                 </svg>
//                 Discord
//               </a>
//             </li>
//             <li>
//               <a href="https://x.com/vite_js" target="_blank">
//                 <svg
//                   className="button-icon"
//                   role="presentation"
//                   aria-hidden="true"
//                 >
//                   <use href="/icons.svg#x-icon"></use>
//                 </svg>
//                 X.com
//               </a>
//             </li>
//             <li>
//               <a href="https://bsky.app/profile/vite.dev" target="_blank">
//                 <svg
//                   className="button-icon"
//                   role="presentation"
//                   aria-hidden="true"
//                 >
//                   <use href="/icons.svg#bluesky-icon"></use>
//                 </svg>
//                 Bluesky
//               </a>
//             </li>
//           </ul>
//         </div>
//       </section>

//       <div className="ticks"></div>
//       <section id="spacer"></section>
//     </>
//   )
// }

// export default App
