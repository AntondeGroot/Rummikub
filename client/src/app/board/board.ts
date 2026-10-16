import { Component } from '@angular/core';
import { PlayerList } from '../player-list/player-list';

@Component({
  imports: [PlayerList],
  selector: 'app-board',
  styleUrl: './board.css',
  templateUrl: './board.html',
})
export class Board {}
