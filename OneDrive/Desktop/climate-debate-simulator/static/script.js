let transcript = [];

async function startDebate(){

    const topic = document.getElementById("topic").value.trim();
    const rounds = parseInt(document.getElementById("rounds").value);

    if(!topic){ alert("Enter topic"); return; }

    document.getElementById("debate-area").innerHTML =
        "<div class='loader'>🤖 AI agents are debating...</div>";

    const res = await fetch("/debate/start",{
        method:"POST",
        headers:{"Content-Type":"application/json"},
        body:JSON.stringify({topic,rounds})
    });

    const data = await res.json();
    transcript = data.messages;

    displayDebate(transcript);
    generateJudgeSummary();
}

function displayDebate(messages){

    const area = document.getElementById("debate-area");
    area.innerHTML = "";

    messages.forEach(msg=>{
        const card = document.createElement("div");

        let agentClass = msg.agent==="USA"?"usa":
                         msg.agent==="EU"?"eu":"china";

        card.className=`card ${agentClass}`;

        card.innerHTML=`
            <div>
              <strong>Round ${msg.round} — ${msg.agent}</strong>
              <span class="badge ${msg.stance}">${msg.stance}</span>
            </div>

            <div class="typing">${msg.message}</div>

            <div class="timestamp">
              ${new Date(msg.timestamp).toLocaleString()}
            </div>
        `;

        area.appendChild(card);
    });

    window.scrollTo(0,document.body.scrollHeight);
}

function generateJudgeSummary(){

    const area = document.getElementById("debate-area");

    const judgeCard = document.createElement("div");
    judgeCard.className="card";

    judgeCard.innerHTML=`
        <strong>🧠 AI Judge Summary</strong>
        <p>The debate shows contrasting priorities among USA, EU, and China.
        EU pushes aggressive climate commitments, USA balances economy and environment,
        while China emphasizes development. Collaboration is essential.</p>
    `;

    area.appendChild(judgeCard);
}

function copyTranscript(){
    navigator.clipboard.writeText(JSON.stringify(transcript,null,2));
    alert("Copied!");
}

function downloadTranscript(){
    const blob = new Blob([JSON.stringify(transcript,null,2)],
                          {type:"application/json"});
    const url = URL.createObjectURL(blob);
    const a=document.createElement("a");
    a.href=url;
    a.download="debate_transcript.json";
    a.click();
}

function clearDebate(){
    document.getElementById("debate-area").innerHTML="";
}