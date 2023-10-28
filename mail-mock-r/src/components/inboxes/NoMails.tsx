export function NoMails({search}: { search: string }) {
    if (search) return <div className={"inboxes-empty"}>No emails match for: <code>{search}</code></div>
    else return <div className={"inboxes-empty"}>No Emails</div>;
}