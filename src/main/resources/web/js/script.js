// js/script.js

document.addEventListener('DOMContentLoaded', () => {
  const params = new URLSearchParams(window.location.search);
  const factionName = params.get('factionName');
  if (!factionName) {
    alert("Укажите параметр ?factionName=YourFactionName в URL");
    return;
  }

  fetch(`/api/factions?factionName=${encodeURIComponent(factionName)}`, { cache: 'no-store' })
    .then(res => res.json())
    .then(data => {
      if (data.error) {
        alert(data.error);
        return;
      }

      // --- Faction Info ---
      document.getElementById('factionName').textContent   = data.factionName;
      document.getElementById('factionLeader').textContent = data.leaderName;
      document.getElementById('memberCount').textContent   = data.memberCount;
      document.getElementById('creationDate').textContent  = data.creationDate;

      // --- Member List ---
      const memberList = document.getElementById('member-list');
      memberList.innerHTML = '';
      data.members.forEach(name => {
        const li = document.createElement('li');
        li.className = "text-gray-700 border-b pb-1";
        li.textContent = name;
        memberList.appendChild(li);
      });

      // --- Last Online Members ---
      const lastList = document.getElementById('last-online-list');
      lastList.innerHTML = '';
      data.lastOnline.forEach(item => {
        const li = document.createElement('li');
        li.className = "text-gray-700 border-b pb-1";
        li.textContent = `${item.name} — Last Online: ${item.lastOnline}`;
        lastList.appendChild(li);
      });

      // --- Additional Statistics ---
      // Total Player Joins Today
      document.getElementById('joinsToday').textContent = data.joinsToday;

      // Most Active Member
      const most = data.mostActiveMember || { name: "-", seconds: 0 };
      const secMost = parseInt(most.seconds, 10) || 0;
      const hMost = Math.floor(secMost / 3600);
      const mMost = Math.floor((secMost % 3600) / 60);
      document.getElementById('mostActiveMember').textContent =
        `${most.name} (${hMost}h ${mMost}m)`;

      // Average Online Time per Player
      const avgSec = parseInt(data.averageOnlineTime, 10) || 0;
      const hAvg = Math.floor(avgSec / 3600);
      const mAvg = Math.floor((avgSec % 3600) / 60);
      document.getElementById('averageOnlineTime').textContent =
        `${hAvg}h ${mAvg}m`;

      // --- Activity Chart: Hours Played per Day ---
      const ctx = document.getElementById('memberChart').getContext('2d');
      const labels    = data.dailyPlaytime.map(d => d.date);
      const hoursData = data.dailyPlaytime.map(d =>
        // convert seconds to hours, round to 2 decimals
        Math.round((d.seconds / 3600) * 100) / 100
      );

      new Chart(ctx, {
        type: 'bar',
        data: {
          labels,
          datasets: [{
            label: 'Hours Played per Day',
            data: hoursData,
            borderWidth: 1,
            backgroundColor: 'rgba(59,130,246,0.5)',
            borderColor:     'rgba(59,130,246,1)'
          }]
        },
        options: {
          plugins: {
            legend: { display: true },
            tooltip: {
              callbacks: {
                label: ctx => ` ${ctx.parsed.y} h`
              }
            }
          },
          scales: {
            x: {
              title: { display: true, text: 'Date' }
            },
            y: {
              beginAtZero: true,
              title: { display: true, text: 'Hours' }
            }
          }
        }
      });
    })
    .catch(err => {
      console.error('Error fetching faction data:', err);
      alert("Ошибка при получении данных фракции");
    });
});
